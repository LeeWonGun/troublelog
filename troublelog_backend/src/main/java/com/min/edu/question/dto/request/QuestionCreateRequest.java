package com.min.edu.question.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 질문 생성 API에서 클라이언트가 전달하는 요청 DTO입니다.
 */
public record QuestionCreateRequest(
        @NotBlank(message = "질문 제목을 입력해 주세요.")
        @Size(max = 200, message = "질문 제목은 200자 이하로 입력해 주세요.")
        String title,

        // 상황 설명만 받고, codeLanguage/code는 QuestionContentFormatter에서 content에 합쳐 저장합니다.
        @NotBlank(message = "질문 내용을 입력해 주세요.")
        String content,

        // 코드 입력이 있을 때만 사용합니다.
        @Size(max = 50, message = "코드 언어는 50자 이하로 입력해 주세요.")
        String codeLanguage,

        // 선택 입력값입니다. 값이 없으면 content만 저장합니다.
        String code,
        String errorMessage,
        String environment,
        String tried,

        @Pattern(regexp = "(?i)PUBLIC|TEAM", message = "공개 범위는 PUBLIC 또는 TEAM만 가능합니다.")
        String visibility,

        @Positive(message = "팀 ID는 양수여야 합니다.")
        Long teamId,

        List<@Positive(message = "기술 스택 ID는 양수여야 합니다.") Long> techStackIds
) {
}
