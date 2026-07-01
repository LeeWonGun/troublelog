package com.min.edu.common.response;

import java.util.List;

/**
 * 목록 API의 페이징 응답을 통일하기 위한 공통 DTO이다.
 *
 * 프론트는 content와 페이지 정보를 함께 받아서
 * 페이지네이션 UI를 구성할 수 있다.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    /**
     * 직접 계산한 페이징 정보로 PageResponse를 생성한다.
     */
    public static <T> PageResponse<T> of(
            List<T> content,
            int page,
            int size,
            long totalElements
    ) {
        int totalPages = calculateTotalPages(totalElements, size);
        boolean hasNext = page + 1 < totalPages;

        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                hasNext
        );
    }

    private static int calculateTotalPages(long totalElements, int size) {
        if (size <= 0) {
            return 0;
        }

        return (int) Math.ceil((double) totalElements / size);
    }
}