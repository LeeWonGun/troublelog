package com.min.edu.techstack.dto.response;

public record TechStackResponse(
        Long techStackId,
        String name,

        // 프론트에서 기술 스택을 분류해서 보여줄 때 사용한다.
        String category
) {
}