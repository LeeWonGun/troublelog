package com.min.edu.question.dto.request;

import java.util.List;

/**
 * 질문 검색 조건을 Service로 전달하기 위한 DTO이다.
 *
 * Controller에서 받은 query parameter를 하나의 객체로 묶어서
 * Service에 전달한다.
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