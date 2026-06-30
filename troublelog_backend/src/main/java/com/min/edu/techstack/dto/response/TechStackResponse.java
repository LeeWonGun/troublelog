package com.min.edu.techstack.dto.response;

// 기술 스택 정보를 프론트로 내려줄 때 사용하는 응답 DTO
public record TechStackResponse(
        Long techStackId,
        String name,

        // 프론트에서 기술 스택을 분류해서 보여줄 때 사용한다.
        String category
) {
}