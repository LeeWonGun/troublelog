package com.min.edu.question.dto.request;

import java.util.List;

// 질문 수정 요청을 받을 때 사용하는 DTO
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