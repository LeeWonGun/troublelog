package com.min.edu.question.service;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.common.response.PageResponse;
import com.min.edu.question.dto.request.QuestionCreateRequest;
import com.min.edu.question.dto.request.QuestionSearchCondition;
import com.min.edu.question.dto.request.QuestionUpdateRequest;
import com.min.edu.question.dto.response.QuestionCreateResponse;
import com.min.edu.question.dto.response.QuestionDetailResponse;
import com.min.edu.question.dto.response.QuestionListResponse;
import com.min.edu.question.dto.response.QuestionSearchRow;
import com.min.edu.question.entity.Question;
import com.min.edu.question.entity.QuestionVisibility;
import com.min.edu.question.mapper.QuestionSearchMapper;
import com.min.edu.question.repository.QuestionRepository;
import com.min.edu.question.service.condition.QuestionListCondition;
import com.min.edu.question.service.condition.QuestionSearchCriteria;
import com.min.edu.question.util.QuestionContentFormatter;
import com.min.edu.question.util.QuestionContentParts;
import com.min.edu.team.repository.TeamMemberRepository;
import com.min.edu.user.domain.User;
import com.min.edu.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 질문 게시글의 비즈니스 흐름과 권한 검증을 담당하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final String DELFLAG_NORMAL = "N";

    private final QuestionRepository questionRepository;
    private final QuestionSearchMapper questionSearchMapper;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final QuestionTechStackService questionTechStackService;
    private final QuestionResponseAssembler questionResponseAssembler;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public PageResponse<QuestionListResponse> getPublicQuestions(
            Integer page,
            Integer size,
            String sort
    ) {
        QuestionListCondition condition = QuestionListCondition.from(page, size, sort);
        Page<Question> questionPage;

        if (condition.status() != null) {
            questionPage = questionRepository.findByVisibilityAndDelflagAndStatus(
                    QuestionVisibility.PUBLIC,
                    DELFLAG_NORMAL,
                    condition.status(),
                    condition.pageable()
            );
        } else {
            questionPage = questionRepository.findByVisibilityAndDelflag(
                    QuestionVisibility.PUBLIC,
                    DELFLAG_NORMAL,
                    condition.pageable()
            );
        }

        return questionResponseAssembler.toPageResponse(questionPage, condition.page(), condition.size());
    }

    @Transactional
    public QuestionDetailResponse getQuestionDetail(Long questionId, Long currentUserId) {
        Question question = getQuestion(questionId);
        validateQuestionAccess(question, currentUserId);

        questionRepository.increaseViewCount(question.getId());
        entityManager.refresh(question);

        QuestionContentParts contentParts = QuestionContentFormatter.parse(question.getContent());
        return questionResponseAssembler.toDetailResponse(question, contentParts, currentUserId);
    }

    @Transactional(readOnly = true)
    public PageResponse<QuestionListResponse> searchPublicQuestions(QuestionSearchCondition condition) {
        return searchQuestions(null, condition);
    }

    @Transactional(readOnly = true)
    public PageResponse<QuestionListResponse> searchTeamQuestions(
            Long currentUserId,
            Long teamId,
            QuestionSearchCondition condition
    ) {
        validateTeamMember(teamId, currentUserId);
        return searchQuestions(teamId, condition);
    }

    @Transactional
    public QuestionCreateResponse createQuestion(Long currentUserId, QuestionCreateRequest request) {
        validateActiveUser(currentUserId);

        QuestionVisibility visibility = normalizeVisibility(request.visibility());
        Long teamId = resolveTeamIdForWrite(visibility, request.teamId(), currentUserId);

        validateRequiredText(request.title(), "질문 제목을 입력해 주세요.");
        validateRequiredText(request.content(), "질문 내용을 입력해 주세요.");

        String storedContent = QuestionContentFormatter.compose(
                request.content(),
                request.codeLanguage(),
                request.code()
        );

        Question question = new Question(
                currentUserId,
                teamId,
                request.title().trim(),
                storedContent,
                trimToNull(request.errorMessage()),
                trimToNull(request.environment()),
                trimToNull(request.tried()),
                visibility
        );

        Question savedQuestion = questionRepository.save(question);
        questionTechStackService.saveTechStacks(savedQuestion.getId(), request.techStackIds());

        return new QuestionCreateResponse(savedQuestion.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<QuestionListResponse> getMyQuestions(
            Long currentUserId,
            Integer page,
            Integer size,
            String sort
    ) {
        validateActiveUser(currentUserId);

        QuestionListCondition condition = QuestionListCondition.from(page, size, sort);
        Page<Question> questionPage;

        if (condition.status() != null) {
            questionPage = questionRepository.findByWriterIdAndDelflagAndStatus(
                    currentUserId,
                    DELFLAG_NORMAL,
                    condition.status(),
                    condition.pageable()
            );
        } else {
            questionPage = questionRepository.findByWriterIdAndDelflag(
                    currentUserId,
                    DELFLAG_NORMAL,
                    condition.pageable()
            );
        }

        return questionResponseAssembler.toPageResponse(questionPage, condition.page(), condition.size());
    }

    @Transactional
    public void updateQuestion(Long currentUserId, Long questionId, QuestionUpdateRequest request) {
        Question question = getQuestion(questionId);
        validateQuestionWriter(question, currentUserId);

        QuestionVisibility visibility = normalizeVisibility(request.visibility());
        Long teamId = resolveTeamIdForWrite(visibility, request.teamId(), currentUserId);

        validateRequiredText(request.title(), "질문 제목을 입력해 주세요.");
        validateRequiredText(request.content(), "질문 내용을 입력해 주세요.");

        String storedContent = QuestionContentFormatter.compose(
                request.content(),
                request.codeLanguage(),
                request.code()
        );

        question.update(
                teamId,
                request.title().trim(),
                storedContent,
                trimToNull(request.errorMessage()),
                trimToNull(request.environment()),
                trimToNull(request.tried()),
                visibility
        );

        questionTechStackService.replaceTechStacks(question.getId(), request.techStackIds());
    }

    @Transactional
    public void deleteQuestion(Long currentUserId, Long questionId) {
        Question question = getQuestion(questionId);
        validateQuestionWriter(question, currentUserId);
        question.delete();
    }

    @Transactional(readOnly = true)
    public PageResponse<QuestionListResponse> getTeamQuestions(
            Long currentUserId,
            Long teamId,
            Integer page,
            Integer size,
            String sort
    ) {
        validateTeamMember(teamId, currentUserId);

        QuestionListCondition condition = QuestionListCondition.from(page, size, sort);
        Page<Question> questionPage;

        if (condition.status() != null) {
            questionPage = questionRepository.findByTeamIdAndVisibilityAndDelflagAndStatus(
                    teamId,
                    QuestionVisibility.TEAM,
                    DELFLAG_NORMAL,
                    condition.status(),
                    condition.pageable()
            );
        } else {
            questionPage = questionRepository.findByTeamIdAndVisibilityAndDelflag(
                    teamId,
                    QuestionVisibility.TEAM,
                    DELFLAG_NORMAL,
                    condition.pageable()
            );
        }

        return questionResponseAssembler.toPageResponse(questionPage, condition.page(), condition.size());
    }

    private PageResponse<QuestionListResponse> searchQuestions(
            Long teamId,
            QuestionSearchCondition condition
    ) {
        QuestionSearchCriteria criteria = QuestionSearchCriteria.from(condition);

        List<QuestionSearchRow> rows = questionSearchMapper.searchQuestions(
                teamId,
                criteria.keyword(),
                criteria.statusName(),
                criteria.techStackIds(),
                criteria.sortName(),
                criteria.offset(),
                criteria.size()
        );

        long totalElements = questionSearchMapper.countSearchQuestions(
                teamId,
                criteria.keyword(),
                criteria.statusName(),
                criteria.techStackIds()
        );

        List<QuestionListResponse> content = questionResponseAssembler.toQuestionListResponsesFromSearchRows(rows);
        return PageResponse.of(content, criteria.page(), criteria.size(), totalElements);
    }

    private Question getQuestion(Long questionId) {
        return questionRepository.findByIdAndDelflag(questionId, DELFLAG_NORMAL)
                .orElseThrow(() -> new BusinessException("질문을 찾을 수 없습니다.", ErrorCode.QUESTION_NOT_FOUND));
    }

    private void validateQuestionAccess(Question question, Long currentUserId) {
        if (question.isPublicQuestion()) {
            return;
        }

        if (question.isTeamQuestion()) {
            if (currentUserId == null) {
                throw new BusinessException("로그인이 필요합니다.", ErrorCode.UNAUTHORIZED);
            }

            validateTeamMember(question.getTeamId(), currentUserId);
            return;
        }

        throw new BusinessException("질문 공개 범위가 올바르지 않습니다.", ErrorCode.INVALID_REQUEST);
    }

    private void validateQuestionWriter(Question question, Long currentUserId) {
        validateActiveUser(currentUserId);

        if (!question.isWriter(currentUserId)) {
            throw new BusinessException("질문 작성자만 수정 또는 삭제할 수 있습니다.", ErrorCode.FORBIDDEN);
        }
    }

    private void validateActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new BusinessException("사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }
    }

    private Long resolveTeamIdForWrite(QuestionVisibility visibility, Long teamId, Long currentUserId) {
        if (QuestionVisibility.PUBLIC.equals(visibility)) {
            return null;
        }

        if (teamId == null) {
            throw new BusinessException("팀 질문은 teamId가 필요합니다.", ErrorCode.INVALID_REQUEST);
        }

        validateTeamMember(teamId, currentUserId);
        return teamId;
    }

    private void validateTeamMember(Long teamId, Long userId) {
        if (teamId == null || userId == null) {
            throw new BusinessException("팀원만 접근할 수 있습니다.", ErrorCode.FORBIDDEN);
        }

        boolean joined = teamMemberRepository.existsActiveMemberInActiveTeam(teamId, userId);

        if (!joined) {
            throw new BusinessException("팀원만 접근할 수 있습니다.", ErrorCode.FORBIDDEN);
        }
    }

    private QuestionVisibility normalizeVisibility(String visibility) {
        if (visibility == null || visibility.isBlank()) {
            return QuestionVisibility.PUBLIC;
        }

        String normalizedVisibility = visibility.trim().toUpperCase();

        if (!QuestionVisibility.PUBLIC.name().equals(normalizedVisibility)
                && !QuestionVisibility.TEAM.name().equals(normalizedVisibility)) {
            throw new BusinessException("질문 공개 범위가 올바르지 않습니다.", ErrorCode.INVALID_REQUEST);
        }

        return QuestionVisibility.valueOf(normalizedVisibility);
    }

    private void validateRequiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message, ErrorCode.INVALID_REQUEST);
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
