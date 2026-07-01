package com.min.edu.question.service;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
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

    private static final String SORT_LATEST = "latest";
    private static final String SORT_POPULAR = "popular";

    private final QuestionRepository questionRepository;
    private final QuestionTechStackRepository questionTechStackRepository;
    private final QuestionSearchMapper questionSearchMapper;

    /**
     * 비회원도 볼 수 있는 공개 질문 목록을 조회한다.
     *
     * PUBLIC 질문만 조회하고, delflag = 'N'인 정상 질문만 반환한다.
     */
    @Transactional(readOnly = true)
    public List<QuestionListResponse> getPublicQuestions() {
        List<Question> questions = questionRepository
                .findByVisibilityAndDelflagOrderByCreatedAtDesc(VISIBILITY_PUBLIC, DELFLAG_NORMAL);

        return toQuestionListResponses(questions);
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
     * 공개 질문을 검색한다.
     *
     * 검색 SQL은 조건 조합이 많기 때문에 MyBatis Mapper에서 처리한다.
     *
     * 검색 조건:
     * - keyword: 제목 또는 본문 검색
     * - status: UNSOLVED 또는 SOLVED
     * - techStackIds: 선택된 기술 스택 중 하나라도 연결된 질문 검색
     * - sort: latest 또는 popular
     */
    @Transactional(readOnly = true)
    public List<QuestionListResponse> searchPublicQuestions(QuestionSearchCondition condition) {
        String keyword = normalizeKeyword(condition.keyword());
        String status = normalizeStatus(condition.status());
        String sort = normalizeSort(condition.sort());
        List<Long> techStackIds = normalizeTechStackIds(condition.techStackIds());

        List<QuestionSearchRow> rows = questionSearchMapper.searchPublicQuestions(
                keyword,
                status,
                techStackIds,
                sort
        );

        return toQuestionListResponsesFromSearchRows(rows);
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

    /**
     * Question Entity 목록을 질문 목록 응답 DTO 목록으로 변환한다.
     *
     * 공개 질문 목록 조회처럼 JPA Entity를 조회한 경우 사용한다.
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
     *
     * 검색 API처럼 Entity가 아니라 조회 전용 DTO로 결과를 받은 경우 사용한다.
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
     *
     * 목록 조회와 검색 조회에서 공통으로 사용한다.
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
     *
     * 질문 상세 화면에서 기술 스택 태그를 보여줄 때 사용한다.
     */
    private List<TechStackResponse> getTechStacks(Long questionId) {
        return questionTechStackRepository.findByQuestionId(questionId)
                .stream()
                .map(questionTechStack -> TechStackResponse.from(questionTechStack.getTechStack()))
                .toList();
    }

    /**
     * keyword 검색 조건을 정리한다.
     *
     * null 또는 공백만 있는 값은 검색 조건에서 제외하기 위해 빈 문자열로 변환한다.
     */
    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    /**
     * status 검색 조건을 정리한다.
     *
     * 값이 없으면 전체 상태를 조회하고,
     * 값이 있으면 UNSOLVED 또는 SOLVED만 허용한다.
     */
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

    /**
     * sort 검색 조건을 정리한다.
     *
     * 값이 없으면 최신순으로 처리하고,
     * latest 또는 popular만 허용한다.
     */
    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return SORT_LATEST;
        }

        String normalizedSort = sort.trim().toLowerCase();

        if (!SORT_LATEST.equals(normalizedSort) && !SORT_POPULAR.equals(normalizedSort)) {
            throw new BusinessException(
                    "정렬 조건이 올바르지 않습니다.",
                    ErrorCode.INVALID_REQUEST
            );
        }

        return normalizedSort;
    }

    /**
     * 기술 스택 검색 조건을 정리한다.
     *
     * null이면 빈 목록으로 바꾸고,
     * 0 이하의 잘못된 ID는 제외한다.
     */
    private List<Long> normalizeTechStackIds(List<Long> techStackIds) {
        if (techStackIds == null) {
            return Collections.emptyList();
        }

        return techStackIds.stream()
                .filter(techStackId -> techStackId != null && techStackId > 0)
                .distinct()
                .toList();
    }
}