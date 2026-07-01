package com.min.edu.question.controller;

import com.min.edu.common.response.ApiResponse;
import com.min.edu.common.response.PageResponse;
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
     * 비회원도 볼 수 있는 공개 질문 목록을 페이징 조회한다.
     *
     * 요청 예:
     * /api/questions/public?page=0&size=5&sort=LATEST
     * /api/questions/public?page=0&size=5&sort=POPULAR
     * /api/questions/public?page=0&size=5&sort=SOLVED
     * /api/questions/public?page=0&size=5&sort=UNSOLVED
     */
    @GetMapping("/api/questions/public")
    public ApiResponse<PageResponse<QuestionListResponse>> getPublicQuestions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.success(
                "공개 질문 목록 조회 성공",
                questionService.getPublicQuestions(page, size, sort)
        );
    }

    /**
     * 전체 공개 게시판 질문을 검색한다.
     *
     * teamId가 없는 경우 PUBLIC 질문만 검색한다.
     */
    @GetMapping("/api/questions/search")
    public ApiResponse<PageResponse<QuestionListResponse>> searchPublicQuestions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<Long> techStackIds,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        QuestionSearchCondition condition = new QuestionSearchCondition(
                keyword,
                status,
                techStackIds,
                sort,
                page,
                size
        );

        return ApiResponse.success(
                "공개 질문 검색 성공",
                questionService.searchQuestions(null, condition)
        );
    }

    /**
     * 팀 게시판 질문을 검색한다.
     *
     * 현재는 teamId 기준 검색 구조만 열어두고,
     * 팀원 권한 검증은 인증/팀 구조가 확정된 뒤 Service에 추가한다.
     */
    @GetMapping("/api/teams/{teamId}/questions/search")
    public ApiResponse<PageResponse<QuestionListResponse>> searchTeamQuestions(
            @PathVariable Long teamId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<Long> techStackIds,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        QuestionSearchCondition condition = new QuestionSearchCondition(
                keyword,
                status,
                techStackIds,
                sort,
                page,
                size
        );

        return ApiResponse.success(
                "팀 질문 검색 성공",
                questionService.searchQuestions(teamId, condition)
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