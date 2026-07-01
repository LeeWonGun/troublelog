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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 질문 게시글 API를 제공하는 Controller이다.
 */
@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 비회원도 볼 수 있는 공개 질문 목록을 페이징 조회한다.
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
                questionService.searchPublicQuestions(condition)
        );
    }

    /**
     * 팀 게시판 질문을 검색한다.
     */
    @GetMapping("/api/teams/{teamId}/questions/search")
    public ApiResponse<PageResponse<QuestionListResponse>> searchTeamQuestions(
            Authentication authentication,
            @PathVariable Long teamId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<Long> techStackIds,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        Long currentUserId = CurrentUser.id(authentication);

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
                questionService.searchTeamQuestions(currentUserId, teamId, condition)
        );
    }

    /**
     * 질문 상세 정보를 조회한다.
     *
     * PUBLIC 질문은 비회원도 조회 가능하고,
     * TEAM 질문은 로그인한 팀원만 조회 가능하다.
     */
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

    /**
     * 로그인 사용자가 PUBLIC 또는 TEAM 질문을 작성한다.
     */
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

    /**
     * 작성자 본인이 질문을 수정한다.
     */
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

    /**
     * 작성자 본인이 질문을 삭제한다.
     */
    @DeleteMapping("/api/questions/{questionId}")
    public ApiResponse<Void> deleteQuestion(
            Authentication authentication,
            @PathVariable Long questionId
    ) {
        Long currentUserId = CurrentUser.id(authentication);

        questionService.deleteQuestion(currentUserId, questionId);

        return ApiResponse.success("질문 삭제 성공");
    }

    /**
     * 로그인 사용자가 자신이 작성한 질문 목록을 조회한다.
     */
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

    /**
     * 팀원이 해당 팀의 TEAM 질문 목록을 조회한다.
     */
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