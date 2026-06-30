package com.min.edu.question.dto.request;

import java.util.List;

// 검색 조건을 Service로 넘기기 위해 묶어둔 조건 DTO
public record QuestionSearchCondition(
        // 제목 또는 내용 검색에 사용한다.
        String keyword,

        // UNSOLVED 또는 SOLVED
        String status,

        // 선택한 기술 스택 조건으로 질문을 필터링한다.
        List<Long> techStackIds,

        // popular이면 likeCount 기준으로 정렬한다.
        String sort
) {
}