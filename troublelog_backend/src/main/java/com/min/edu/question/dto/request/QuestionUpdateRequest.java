package com.min.edu.question.dto.request;

import java.util.List;

public record QuestionUpdateRequest(
        String title,

        // 상황 설명만 받는다. 코드는 codeLanguage, code와 조합해 questions.content에 저장한다.
        String content,

        // 코드 입력이 있을 때만 사용한다.
        String codeLanguage,

        // 선택 입력값이다. 값이 없으면 content만 저장한다.
        String code,

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