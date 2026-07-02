package com.min.edu.question.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionUpdateRequest(
        @NotBlank(message = "Question title is required.")
        @Size(max = 200, message = "Question title must be 200 characters or less.")
        String title,

        // 상황 설명만 받는다. 코드는 codeLanguage, code와 조합해 questions.content에 저장한다.
        @NotBlank(message = "Question content is required.")
        String content,

        // 코드 입력이 있을 때만 사용한다.
        @Size(max = 50, message = "Code language must be 50 characters or less.")
        String codeLanguage,

        // 선택 입력값이다. 값이 없으면 content만 저장한다.
        String code,
        String errorMessage,
        String environment,
        String tried,

        // PUBLIC 또는 TEAM
        @Pattern(regexp = "(?i)PUBLIC|TEAM", message = "Visibility must be PUBLIC or TEAM.")
        String visibility,

        // TEAM 질문으로 수정할 때만 필요하다.
        @Positive(message = "Team id must be positive.")
        Long teamId,

        // 수정 후 질문에 연결할 기술 스택 ID 목록이다.
        List<@Positive(message = "Tech stack id must be positive.") Long> techStackIds
) {
}
