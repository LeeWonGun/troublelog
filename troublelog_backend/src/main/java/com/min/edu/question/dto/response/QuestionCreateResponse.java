package com.min.edu.question.dto.response;

/**
 * 질문 생성 성공 후 생성된 질문 ID를 반환하는 응답 DTO입니다.
 */
public record QuestionCreateResponse(
        Long questionId
) {
}
