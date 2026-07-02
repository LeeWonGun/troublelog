package com.min.edu.question.service.condition;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.question.entity.QuestionSort;
import com.min.edu.question.entity.QuestionStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 질문 목록 조회의 페이지, 정렬, 상태 필터 조건을 정규화하는 내부 조건 객체입니다.
 */
public record QuestionListCondition(
        int page,
        int size,
        QuestionSort sort,
        QuestionStatus status
) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    public static QuestionListCondition from(Integer page, Integer size, String sort) {
        QuestionSortOption option = QuestionSortOption.from(sort);
        return new QuestionListCondition(
                normalizePage(page),
                normalizeSize(size),
                option.sort(),
                option.status()
        );
    }

    public Pageable pageable() {
        if (QuestionSort.POPULAR == sort) {
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

    static int normalizePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }

        if (page < 0) {
            throw new BusinessException("페이지 번호는 0 이상이어야 합니다.", ErrorCode.INVALID_REQUEST);
        }

        return page;
    }

    static int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        if (size <= 0) {
            throw new BusinessException("페이지 크기는 1 이상이어야 합니다.", ErrorCode.INVALID_REQUEST);
        }

        return Math.min(size, MAX_SIZE);
    }
}
