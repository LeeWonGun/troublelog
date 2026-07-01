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
import com.min.edu.question.entity.QuestionTechStack;
import com.min.edu.question.mapper.QuestionSearchMapper;
import com.min.edu.question.repository.QuestionRepository;
import com.min.edu.question.repository.QuestionTechStackRepository;
import com.min.edu.question.util.QuestionContentFormatter;
import com.min.edu.question.util.QuestionContentParts;
import com.min.edu.team.repository.TeamMemberRepository;
import com.min.edu.techstack.dto.response.TechStackResponse;
import com.min.edu.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 질문 게시글 관련 비즈니스 로직을 담당하는 Service이다.
 *
 * 단순 Entity 조회/저장/수정/삭제는 JPA Repository를 사용하고,
 * 조건이 많은 검색 조회는 MyBatis Mapper를 사용한다.
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final String VISIBILITY_PUBLIC = "PUBLIC";
    private static final String VISIBILITY_TEAM = "TEAM";
    private static final String DELFLAG_NORMAL = "N";

    private static final String STATUS_UNSOLVED = "UNSOLVED";
    private static final String STATUS_SOLVED = "SOLVED";

    private static final String SORT_LATEST = "LATEST";
    private static final String SORT_POPULAR = "POPULAR";

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    private final QuestionRepository questionRepository;
    private final QuestionTechStackRepository questionTechStackRepository;
    private final QuestionSearchMapper questionSearchMapper;
    private final TeamMemberRepository teamMemberRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public PageResponse<QuestionListResponse> getPublicQuestions(
            Integer page,
            Integer size,
            String sort
    ) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedSort = normalizeSort(sort);

        Pageable pageable = createPageable(normalizedPage, normalizedSize, normalizedSort);

        Page<Question> questionPage;

        if (STATUS_SOLVED.equals(normalizedSort) || STATUS_UNSOLVED.equals(normalizedSort)) {
            questionPage = questionRepository.findByVisibilityAndDelflagAndStatus(
                    VISIBILITY_PUBLIC,
                    DELFLAG_NORMAL,
                    normalizedSort,
                    pageable
            );
        } else {
            questionPage = questionRepository.findByVisibilityAndDelflag(
                    VISIBILITY_PUBLIC,
                    DELFLAG_NORMAL,
                    pageable
            );
        }

        return toPageResponse(questionPage, normalizedPage, normalizedSize);
    }

    @Transactional
    public QuestionDetailResponse getQuestionDetail(Long questionId, Long currentUserId) {
        Question question = getQuestion(questionId);

        validateQuestionAccess(question, currentUserId);

        question.increaseViewCount();

        QuestionContentParts contentParts = QuestionContentFormatter.parse(question.getContent());
        List<TechStackResponse> techStacks = getTechStacks(question.getId());

        return QuestionDetailResponse.from(question, contentParts, techStacks);
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
        userService.getActiveUser(currentUserId);

        String visibility = normalizeVisibility(request.visibility());
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
        saveTechStacks(savedQuestion.getId(), request.techStackIds());

        return new QuestionCreateResponse(savedQuestion.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<QuestionListResponse> getMyQuestions(
            Long currentUserId,
            Integer page,
            Integer size,
            String sort
    ) {
        userService.getActiveUser(currentUserId);

        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedSort = normalizeSort(sort);

        Pageable pageable = createPageable(normalizedPage, normalizedSize, normalizedSort);

        Page<Question> questionPage;

        if (STATUS_SOLVED.equals(normalizedSort) || STATUS_UNSOLVED.equals(normalizedSort)) {
            questionPage = questionRepository.findByWriterIdAndDelflagAndStatus(
                    currentUserId,
                    DELFLAG_NORMAL,
                    normalizedSort,
                    pageable
            );
        } else {
            questionPage = questionRepository.findByWriterIdAndDelflag(
                    currentUserId,
                    DELFLAG_NORMAL,
                    pageable
            );
        }

        return toPageResponse(questionPage, normalizedPage, normalizedSize);
    }

    @Transactional
    public void updateQuestion(Long currentUserId, Long questionId, QuestionUpdateRequest request) {
        Question question = getQuestion(questionId);

        validateQuestionWriter(question, currentUserId);

        String visibility = normalizeVisibility(request.visibility());
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

        questionTechStackRepository.deleteByQuestionId(question.getId());
        saveTechStacks(question.getId(), request.techStackIds());
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

        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedSort = normalizeSort(sort);

        Pageable pageable = createPageable(normalizedPage, normalizedSize, normalizedSort);

        Page<Question> questionPage;

        if (STATUS_SOLVED.equals(normalizedSort) || STATUS_UNSOLVED.equals(normalizedSort)) {
            questionPage = questionRepository.findByTeamIdAndVisibilityAndDelflagAndStatus(
                    teamId,
                    VISIBILITY_TEAM,
                    DELFLAG_NORMAL,
                    normalizedSort,
                    pageable
            );
        } else {
            questionPage = questionRepository.findByTeamIdAndVisibilityAndDelflag(
                    teamId,
                    VISIBILITY_TEAM,
                    DELFLAG_NORMAL,
                    pageable
            );
        }

        return toPageResponse(questionPage, normalizedPage, normalizedSize);
    }

    private PageResponse<QuestionListResponse> searchQuestions(
            Long teamId,
            QuestionSearchCondition condition
    ) {
        int page = normalizePage(condition.page());
        int size = normalizeSize(condition.size());
        String keyword = normalizeKeyword(condition.keyword());

        SearchOption searchOption = resolveSearchOption(condition.sort(), condition.status());
        List<Long> techStackIds = normalizeTechStackIds(condition.techStackIds());

        int offset = page * size;

        List<QuestionSearchRow> rows = questionSearchMapper.searchQuestions(
                teamId,
                keyword,
                searchOption.status(),
                techStackIds,
                searchOption.sort(),
                offset,
                size
        );

        long totalElements = questionSearchMapper.countSearchQuestions(
                teamId,
                keyword,
                searchOption.status(),
                techStackIds
        );

        List<QuestionListResponse> content = toQuestionListResponsesFromSearchRows(rows);

        return PageResponse.of(content, page, size, totalElements);
    }

    private PageResponse<QuestionListResponse> toPageResponse(
            Page<Question> questionPage,
            int page,
            int size
    ) {
        return new PageResponse<>(
                toQuestionListResponses(questionPage.getContent()),
                page,
                size,
                questionPage.getTotalElements(),
                questionPage.getTotalPages(),
                questionPage.hasNext()
        );
    }

    private Question getQuestion(Long questionId) {
        return questionRepository.findByIdAndDelflag(questionId, DELFLAG_NORMAL)
                .orElseThrow(() -> new BusinessException(
                        "질문을 찾을 수 없습니다.",
                        ErrorCode.QUESTION_NOT_FOUND
                ));
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
        userService.getActiveUser(currentUserId);

        if (!question.isWriter(currentUserId)) {
            throw new BusinessException("질문 작성자만 수정 또는 삭제할 수 있습니다.", ErrorCode.FORBIDDEN);
        }
    }

    private Long resolveTeamIdForWrite(String visibility, Long teamId, Long currentUserId) {
        if (VISIBILITY_PUBLIC.equals(visibility)) {
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

        boolean joined = teamMemberRepository.existsByTeamIdAndUserIdAndDelflag(
                teamId,
                userId,
                DELFLAG_NORMAL
        );

        if (!joined) {
            throw new BusinessException("팀원만 접근할 수 있습니다.", ErrorCode.FORBIDDEN);
        }
    }

    private void saveTechStacks(Long questionId, List<Long> techStackIds) {
        List<Long> normalizedTechStackIds = normalizeTechStackIds(techStackIds);

        if (normalizedTechStackIds.isEmpty()) {
            return;
        }

        List<QuestionTechStack> questionTechStacks = normalizedTechStackIds.stream()
                .map(techStackId -> new QuestionTechStack(questionId, techStackId))
                .toList();

        questionTechStackRepository.saveAll(questionTechStacks);
    }

    private List<QuestionListResponse> toQuestionListResponses(List<Question> questions) {
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .toList();

        Map<Long, List<TechStackResponse>> techStackMap = getTechStackMap(questionIds);

        return questions.stream()
                .map(question -> QuestionListResponse.from(
                        question,
                        techStackMap.getOrDefault(question.getId(), Collections.emptyList())
                ))
                .toList();
    }

    private List<QuestionListResponse> toQuestionListResponsesFromSearchRows(List<QuestionSearchRow> rows) {
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = rows.stream()
                .map(QuestionSearchRow::questionId)
                .toList();

        Map<Long, List<TechStackResponse>> techStackMap = getTechStackMap(questionIds);

        return rows.stream()
                .map(row -> QuestionListResponse.fromSearchRow(
                        row,
                        techStackMap.getOrDefault(row.questionId(), Collections.emptyList())
                ))
                .toList();
    }

    private Map<Long, List<TechStackResponse>> getTechStackMap(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return questionTechStackRepository.findByQuestionIdIn(questionIds)
                .stream()
                .collect(Collectors.groupingBy(
                        QuestionTechStack::getQuestionId,
                        Collectors.mapping(
                                questionTechStack -> TechStackResponse.from(questionTechStack.getTechStack()),
                                Collectors.toList()
                        )
                ));
    }

    private List<TechStackResponse> getTechStacks(Long questionId) {
        return questionTechStackRepository.findByQuestionId(questionId)
                .stream()
                .map(questionTechStack -> TechStackResponse.from(questionTechStack.getTechStack()))
                .toList();
    }

    private Pageable createPageable(int page, int size, String sort) {
        if (SORT_POPULAR.equals(sort)) {
            return PageRequest.of(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "likeCount")
                            .and(Sort.by(Sort.Direction.DESC, "createdAt"))
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    private int normalizePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }

        if (page < 0) {
            throw new BusinessException("페이지 번호는 0 이상이어야 합니다.", ErrorCode.INVALID_REQUEST);
        }

        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        if (size <= 0) {
            throw new BusinessException("페이지 크기는 1 이상이어야 합니다.", ErrorCode.INVALID_REQUEST);
        }

        return Math.min(size, MAX_SIZE);
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private String normalizeVisibility(String visibility) {
        if (visibility == null || visibility.isBlank()) {
            return VISIBILITY_PUBLIC;
        }

        String normalizedVisibility = visibility.trim().toUpperCase();

        if (!VISIBILITY_PUBLIC.equals(normalizedVisibility) && !VISIBILITY_TEAM.equals(normalizedVisibility)) {
            throw new BusinessException("질문 공개 범위가 올바르지 않습니다.", ErrorCode.INVALID_REQUEST);
        }

        return normalizedVisibility;
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return SORT_LATEST;
        }

        String normalizedSort = sort.trim().toUpperCase();

        if (!SORT_LATEST.equals(normalizedSort)
                && !SORT_POPULAR.equals(normalizedSort)
                && !STATUS_SOLVED.equals(normalizedSort)
                && !STATUS_UNSOLVED.equals(normalizedSort)) {
            throw new BusinessException("정렬 조건이 올바르지 않습니다.", ErrorCode.INVALID_REQUEST);
        }

        return normalizedSort;
    }

    private SearchOption resolveSearchOption(String sort, String status) {
        String normalizedSort = normalizeSort(sort);
        String normalizedStatus = normalizeStatus(status);

        if ((STATUS_SOLVED.equals(normalizedSort) || STATUS_UNSOLVED.equals(normalizedSort))
                && normalizedStatus.isBlank()) {
            return new SearchOption(SORT_LATEST, normalizedSort);
        }

        return new SearchOption(normalizedSort, normalizedStatus);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }

        String normalizedStatus = status.trim().toUpperCase();

        if (!STATUS_UNSOLVED.equals(normalizedStatus) && !STATUS_SOLVED.equals(normalizedStatus)) {
            throw new BusinessException("질문 상태 검색 조건이 올바르지 않습니다.", ErrorCode.INVALID_REQUEST);
        }

        return normalizedStatus;
    }

    private List<Long> normalizeTechStackIds(List<Long> techStackIds) {
        if (techStackIds == null) {
            return Collections.emptyList();
        }

        return techStackIds.stream()
                .filter(techStackId -> techStackId != null && techStackId > 0)
                .distinct()
                .toList();
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

    private record SearchOption(
            String sort,
            String status
    ) {
    }
}