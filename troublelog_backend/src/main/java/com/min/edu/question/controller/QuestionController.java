package com.min.edu.question.controller;

import com.min.edu.common.response.ApiResponse;
import com.min.edu.question.dto.request.QuestionSearchCondition;
import com.min.edu.question.dto.response.QuestionDetailResponse;
import com.min.edu.question.dto.response.QuestionListResponse;
import com.min.edu.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 질문 조회 API를 제공하는 Controller이다.
 */
@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 비회원도 볼 수 있는 공개 질문 목록을 조회한다.
     */
    @GetMapping("/api/questions/public")
    public ApiResponse<List<QuestionListResponse>> getPublicQuestions() {
        return ApiResponse.success(
                "공개 질문 목록 조회 성공",
                questionService.getPublicQuestions()
        );
    }

    /**
     * 공개 질문을 검색한다.
     *
     * 요청 예:
     * /api/questions/search?keyword=Spring
     * /api/questions/search?status=UNSOLVED
     * /api/questions/search?techStackIds=1&techStackIds=2
     * /api/questions/search?sort=popular
     */
    @GetMapping("/api/questions/search")
    public ApiResponse<List<QuestionListResponse>> searchPublicQuestions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<Long> techStackIds,
            @RequestParam(required = false) String sort
    ) {
        QuestionSearchCondition condition = new QuestionSearchCondition(
                keyword,
                status,
                techStackIds,
                sort
        );

        return ApiResponse.success(
                "공개 질문 검색 성공",
                questionService.searchPublicQuestions(condition)
        );
    }

    /**
     * 질문 상세 정보를 조회한다.
     *
     * 현재 단계에서는 PUBLIC 질문만 조회 가능하며,
     * TEAM 질문 권한 검사는 인증/팀 구조 확정 후 추가한다.
     */
    @GetMapping("/api/questions/{questionId}")
    public ApiResponse<QuestionDetailResponse> getQuestionDetail(
            @PathVariable Long questionId
    ) {
        return ApiResponse.success(
                "질문 상세 조회 성공",
                questionService.getQuestionDetail(questionId)
        );
    }
}