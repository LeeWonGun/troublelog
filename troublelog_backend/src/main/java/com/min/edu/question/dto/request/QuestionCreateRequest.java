package com.min.edu.question.dto.request;

import java.util.List;

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