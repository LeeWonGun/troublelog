package com.min.edu.question.service.condition;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.question.entity.QuestionSort;
import com.min.edu.question.entity.QuestionStatus;

/**
 * 질문 정렬 값과 기존 status 기반 sort 호환 값을 함께 해석하는 옵션 객체입니다.
 */
public record QuestionSortOption(
        QuestionSort sort,
        QuestionStatus status
) {

    public static QuestionSortOption from(String sort) {
        if (sort == null || sort.isBlank()) {
            return new QuestionSortOption(QuestionSort.LATEST, null);
        }

        String normalizedSort = sort.trim().toUpperCase();

        if (isQuestionStatus(normalizedSort)) {
            return new QuestionSortOption(QuestionSort.LATEST, QuestionStatus.valueOf(normalizedSort));
        }

        if (isQuestionSort(normalizedSort)) {
            return new QuestionSortOption(QuestionSort.valueOf(normalizedSort), null);
        }

        throw new BusinessException("정렬 조건이 올바르지 않습니다.", ErrorCode.INVALID_REQUEST);
    }

    static boolean isQuestionStatus(String value) {
        try {
            QuestionStatus.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isQuestionSort(String value) {
        try {
            QuestionSort.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
