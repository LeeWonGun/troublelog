package com.min.edu.question.dto.response;

// 질문 작성이 성공했을 때 프론트로 돌려주는 응답 DTO
public record QuestionCreateResponse(
        Long questionId
) {
}