package com.min.edu.question.dto.request;

import java.util.List;

/**
 * 질문 검색 API의 query parameter를 하나로 묶어 전달하는 요청 DTO입니다.
 */
public record QuestionSearchCondition(
        String keyword,
        String status,
        List<Long> techStackIds,
        String sort,
        Integer page,
        Integer size
) {
}
