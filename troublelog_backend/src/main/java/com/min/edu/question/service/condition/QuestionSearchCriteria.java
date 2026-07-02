package com.min.edu.question.service.condition;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.question.dto.request.QuestionSearchCondition;
import com.min.edu.question.entity.QuestionSort;
import com.min.edu.question.entity.QuestionStatus;

import java.util.List;

/**
 * 질문 검색 요청 조건을 Mapper 호출에 필요한 내부 검색 조건으로 변환하는 객체입니다.
 */
public record QuestionSearchCriteria(
        String keyword,
        QuestionStatus status,
        List<Long> techStackIds,
        QuestionSort sort,
        int page,
        int size
) {

    public static QuestionSearchCriteria from(QuestionSearchCondition condition) {
        if (condition == null) {
            return fromValues(null, null, null, null, null, null);
        }

        return fromValues(
                condition.keyword(),
                condition.status(),
                condition.techStackIds(),
                condition.sort(),
                condition.page(),
                condition.size()
        );
    }

    public int offset() {
        return page * size;
    }

    public String sortName() {
        return sort.name();
    }

    public String statusName() {
        return status == null ? "" : status.name();
    }

    private static QuestionSearchCriteria fromValues(
            String keyword,
            String status,
            List<Long> techStackIds,
            String sort,
            Integer page,
            Integer size
    ) {
        QuestionSortOption sortOption = QuestionSortOption.from(sort);
        QuestionStatus normalizedStatus = normalizeStatus(status);

        if (normalizedStatus == null && sortOption.status() != null) {
            normalizedStatus = sortOption.status();
        }

        return new QuestionSearchCriteria(
                normalizeKeyword(keyword),
                normalizedStatus,
                normalizeTechStackIds(techStackIds),
                sortOption.sort(),
                QuestionListCondition.normalizePage(page),
                QuestionListCondition.normalizeSize(size)
        );
    }

    private static String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private static QuestionStatus normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        String normalizedStatus = status.trim().toUpperCase();

        if (!QuestionSortOption.isQuestionStatus(normalizedStatus)) {
            throw new BusinessException("질문 상태 검색 조건이 올바르지 않습니다.", ErrorCode.INVALID_REQUEST);
        }

        return QuestionStatus.valueOf(normalizedStatus);
    }

    private static List<Long> normalizeTechStackIds(List<Long> techStackIds) {
        if (techStackIds == null) {
            return List.of();
        }

        return techStackIds.stream()
                .filter(techStackId -> techStackId != null && techStackId > 0)
                .distinct()
                .toList();
    }
}
