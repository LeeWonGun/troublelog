package com.min.edu.question.dto.request;

import java.util.List;

public record QuestionUpdateRequest(
        String title,
        String content,
        String errorMessage,
        String environment,
        String tried,

        // PUBLIC 또는 TEAM
        String visibility,

        // TEAM 질문으로 수정할 때만 필요하다.
        Long teamId,

        // 수정 후 질문에 연결할 기술 스택 ID 목록이다.
        List<Long> techStackIds
) {
}