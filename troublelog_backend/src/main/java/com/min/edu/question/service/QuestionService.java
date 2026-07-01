package com.min.edu.question.service;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.common.response.PageResponse;
import com.min.edu.question.dto.request.QuestionSearchCondition;
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
import com.min.edu.techstack.dto.response.TechStackResponse;
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
 * 질문 조회 관련 비즈니스 로직을 담당하는 Service이다.
 *
 * 단순 Entity 조회는 JPA Repository를 사용하고,
 * 조건이 많은 검색 조회는 MyBatis Mapper를 사용한다.
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final String VISIBILITY_PUBLIC = "PUBLIC";
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

    /**
     * 비회원도 볼 수 있는 공개 질문 목록을 페이징 조회한다.
     *
     * sort:
     * - LATEST: 최신순
     * - POPULAR: 인기순
     * - SOLVED: 해결됨 질문만 최신순
     * - UNSOLVED: 미해결 질문만 최신순
     */
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

        List<QuestionListResponse> content = toQuestionListResponses(questionPage.getContent());

        return new PageResponse<>(
                content,
                normalizedPage,
                normalizedSize,
                questionPage.getTotalElements(),
                questionPage.getTotalPages(),
                questionPage.hasNext()
        );
    }

    /**
     * 질문 상세 정보를 조회한다.
     *
     * 현재 단계에서는 PUBLIC 질문 상세 조회만 허용한다.
     * TEAM 질문 권한 검사는 인증/팀 구조 확정 후 추가한다.
     */
    @Transactional
    public QuestionDetailResponse getQuestionDetail(Long questionId) {
        Question question = questionRepository.findByIdAndDelflag(questionId, DELFLAG_NORMAL)
                .orElseThrow(() -> new BusinessException(
                        "질문을 찾을 수 없습니다.",
                        ErrorCode.QUESTION_NOT_FOUND
                ));

        validatePublicQuestion(question);

        question.increaseViewCount();

        QuestionContentParts contentParts = QuestionContentFormatter.parse(question.getContent());
        List<TechStackResponse> techStacks = getTechStacks(question.getId());

        return QuestionDetailResponse.from(question, contentParts, techStacks);
    }

    /**
     * 공개 게시판 또는 팀 게시판 질문을 검색한다.
     *
     * teamId가 null이면 전체 공개 게시판 검색,
     * teamId가 있으면 해당 팀 질문 검색으로 사용한다.
     *
     * TEAM 질문의 팀원 권한 검증은 인증/팀 구조 확정 후 추가한다.
     */
    @Transactional(readOnly = true)
    public PageResponse<QuestionListResponse> searchQuestions(
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

    /**
     * 공개 질문 상세 조회에서는 PUBLIC 질문만 허용한다.
     */
    private void validatePublicQuestion(Question question) {
        if (!VISIBILITY_PUBLIC.equals(question.getVisibility())) {
            throw new BusinessException(
                    "팀 질문은 팀원만 조회할 수 있습니다.",
                    ErrorCode.FORBIDDEN
            );
        }
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

    /**
     * Question Entity 목록을 질문 목록 응답 DTO 목록으로 변환한다.
     */
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

    /**
     * MyBatis 검색 결과 Row 목록을 질문 목록 응답 DTO 목록으로 변환한다.
     */
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

    /**
     * 질문 ID 목록에 연결된 기술 스택을 questionId 기준으로 묶어서 반환한다.
     */
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

    /**
     * 특정 질문에 연결된 기술 스택 목록을 조회한다.
     */
    private List<TechStackResponse> getTechStacks(Long questionId) {
        return questionTechStackRepository.findByQuestionId(questionId)
                .stream()
                .map(questionTechStack -> TechStackResponse.from(questionTechStack.getTechStack()))
                .toList();
    }

    private int normalizePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }

        if (page < 0) {
            throw new BusinessException(
                    "페이지 번호는 0 이상이어야 합니다.",
                    ErrorCode.INVALID_REQUEST
            );
        }

        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        if (size <= 0) {
            throw new BusinessException(
                    "페이지 크기는 1 이상이어야 합니다.",
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (size > MAX_SIZE) {
            return MAX_SIZE;
        }

        return size;
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
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
            throw new BusinessException(
                    "정렬 조건이 올바르지 않습니다.",
                    ErrorCode.INVALID_REQUEST
            );
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
            throw new BusinessException(
                    "질문 상태 검색 조건이 올바르지 않습니다.",
                    ErrorCode.INVALID_REQUEST
            );
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

    /**
     * 검색용 정렬 조건과 상태 필터를 함께 보관하는 내부 DTO이다.
     */
    private record SearchOption(
            String sort,
            String status
    ) {
    }
}