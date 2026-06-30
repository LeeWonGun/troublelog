package com.min.edu.techstack.dto.response;

import com.min.edu.techstack.entity.TechStack;

/**
 * 기술 스택 목록 조회 API의 응답 DTO이다.
 *
 * Entity 전체를 그대로 노출하지 않고,
 * 프론트 화면에 필요한 값만 내려준다.
 */
public record TechStackResponse(
        Long techStackId,
        String name,

        // 프론트에서 기술 스택을 분류해서 보여줄 때 사용한다.
        String category
) {

    /**
     * TechStack Entity를 API 응답 DTO로 변환한다.
     */
    public static TechStackResponse from(TechStack techStack) {
        return new TechStackResponse(
                techStack.getId(),
                techStack.getName(),
                techStack.getCategory()
        );
    }
}