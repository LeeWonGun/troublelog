package com.min.edu.question.dto.request;

import java.util.List;

// 질문 작성 요청을 받을 때 사용하는 DTO
public record QuestionCreateRequest(
        String title,
        String content,
        String errorMessage,
        String environment,
        String tried,

        // PUBLIC 또는 TEAM
        String visibility,

        // TEAM 질문 작성 시에만 필요하다.
        Long teamId,

        // 질문 작성 시 선택한 기술 스택 ID 목록이다.
        List<Long> techStackIds
) {
}