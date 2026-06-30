package com.min.edu.techstack.controller;

import com.min.edu.common.response.ApiResponse;
import com.min.edu.techstack.dto.response.TechStackResponse;
import com.min.edu.techstack.service.TechStackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 기술 스택 목록 조회 API를 제공하는 Controller이다.
 */
@RestController
@RequiredArgsConstructor
public class TechStackController {

    private final TechStackService techStackService;

    /**
     * 질문 작성 화면과 검색 화면에서 사용할 기술 스택 목록을 조회한다.
     */
    @GetMapping("/api/tech-stacks")
    public ApiResponse<List<TechStackResponse>> getTechStacks() {
        return ApiResponse.success(
                "기술 스택 목록 조회 성공",
                techStackService.getTechStacks()
        );
    }
}