package com.min.edu.question.controller;

import com.min.edu.auth.security.CurrentUser;
import com.min.edu.common.response.ApiResponse;
import com.min.edu.common.response.PageResponse;
import com.min.edu.question.dto.request.QuestionCreateRequest;
import com.min.edu.question.dto.request.QuestionSearchCondition;
import com.min.edu.question.dto.request.QuestionUpdateRequest;
import com.min.edu.question.dto.response.QuestionCreateResponse;
import com.min.edu.question.dto.response.QuestionDetailResponse;
import com.min.edu.question.dto.response.QuestionListResponse;
import com.min.edu.question.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 질문 게시글의 생성, 조회, 검색, 수정, 삭제 API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

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

    @GetMapping("/api/questions/search")
    public ApiResponse<PageResponse<QuestionListResponse>> searchPublicQuestions(
            @ModelAttribute QuestionSearchCondition condition
    ) {
        return ApiResponse.success(
                "공개 질문 검색 성공",
                questionService.searchPublicQuestions(condition)
        );
    }

    @GetMapping("/api/teams/{teamId}/questions/search")
    public ApiResponse<PageResponse<QuestionListResponse>> searchTeamQuestions(
            Authentication authentication,
            @PathVariable Long teamId,
            @ModelAttribute QuestionSearchCondition condition
    ) {
        Long currentUserId = CurrentUser.id(authentication);

        return ApiResponse.success(
                "팀 질문 검색 성공",
                questionService.searchTeamQuestions(currentUserId, teamId, condition)
        );
    }

    @GetMapping("/api/questions/{questionId}")
    public ApiResponse<QuestionDetailResponse> getQuestionDetail(
            Authentication authentication,
            @PathVariable Long questionId
    ) {
        return ApiResponse.success(
                "질문 상세 조회 성공",
                questionService.getQuestionDetail(questionId, nullableCurrentUserId(authentication))
        );
    }

    @PostMapping("/api/questions")
    public ApiResponse<QuestionCreateResponse> createQuestion(
            Authentication authentication,
            @Valid @RequestBody QuestionCreateRequest request
    ) {
        Long currentUserId = CurrentUser.id(authentication);

        return ApiResponse.success(
                "질문 작성 성공",
                questionService.createQuestion(currentUserId, request)
        );
    }

    @GetMapping("/api/questions/popular")
    public ApiResponse<PageResponse<QuestionListResponse>> getPopularQuestions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.success(
                "인기 질문 목록 조회 성공",
                questionService.getPublicQuestions(page, size, "POPULAR")
        );
    }

    @PutMapping("/api/questions/{questionId}")
    public ApiResponse<Void> updateQuestion(
            Authentication authentication,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionUpdateRequest request
    ) {
        Long currentUserId = CurrentUser.id(authentication);

        questionService.updateQuestion(currentUserId, questionId, request);

        return ApiResponse.success("질문 수정 성공");
    }

    @DeleteMapping("/api/questions/{questionId}")
    public ApiResponse<Void> deleteQuestion(
            Authentication authentication,
            @PathVariable Long questionId
    ) {
        Long currentUserId = CurrentUser.id(authentication);

        questionService.deleteQuestion(currentUserId, questionId);

        return ApiResponse.success("질문 삭제 성공");
    }

    @GetMapping("/api/users/me/questions")
    public ApiResponse<PageResponse<QuestionListResponse>> getMyQuestions(
            Authentication authentication,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        Long currentUserId = CurrentUser.id(authentication);

        return ApiResponse.success(
                "내 질문 목록 조회 성공",
                questionService.getMyQuestions(currentUserId, page, size, sort)
        );
    }

    @GetMapping("/api/teams/{teamId}/questions")
    public ApiResponse<PageResponse<QuestionListResponse>> getTeamQuestions(
            Authentication authentication,
            @PathVariable Long teamId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        Long currentUserId = CurrentUser.id(authentication);

        return ApiResponse.success(
                "팀 질문 목록 조회 성공",
                questionService.getTeamQuestions(currentUserId, teamId, page, size, sort)
        );
    }

    private Long nullableCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            return null;
        }

        return userId;
    }
}
