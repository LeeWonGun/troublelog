package com.min.edu.like.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.min.edu.auth.security.CurrentUser;
import com.min.edu.common.response.ApiResponse;
import com.min.edu.like.dto.LikeResponse;
import com.min.edu.like.service.LikeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LikeController {
	
	private final LikeService likeService;
	
	// 질문 좋아요 등록
	@PostMapping("/api/questions/{questionId}/likes")
	public ApiResponse<LikeResponse> likeQuestion(
			@PathVariable Long questionId,
			Authentication authentication
	) {
		Long userId = CurrentUser.id(authentication);
		
		LikeResponse response = likeService.likeQuestion(userId, questionId);
		
		return ApiResponse.success("좋아요가 등록되었습니다", response);
	}
	
	
	// 답변 좋아요 등록 (depth = 0만 가능)
	@PostMapping("/api/answers/{answerId}/likes")
	public ApiResponse<LikeResponse> likeAnswer(
			@PathVariable Long answerId,
			Authentication authentication
	) {
		
		Long userId = CurrentUser.id(authentication);
		
		LikeResponse response = likeService.likeAnswer(userId, answerId);
		
		return ApiResponse.success("좋아요가 등록되었습니다.", response);
	}
	
	
	// 질문 좋아요 취소
	@DeleteMapping("/api/questions/{questionId}/likes")
	public ApiResponse<LikeResponse> unlikeQuestion(
			@PathVariable Long questionId,
			Authentication authentication
			) {
		
		Long userId = CurrentUser.id(authentication);
		
		LikeResponse response = likeService.unlikeQuestion(userId, questionId);
		
		return ApiResponse.success("좋아요가 취소되었습니다.", response);
	}
	
	
	// 답변 좋아요 취소
	@DeleteMapping("/api/answers/{answerId}/likes")
	public ApiResponse<LikeResponse> unlikeAnswer(
			@PathVariable Long answerId,
			Authentication authentication
	) {
		
		Long userId = CurrentUser.id(authentication);
		
		LikeResponse response = likeService.unlikeAnswer(userId, answerId);
		
		return ApiResponse.success("좋아요가 취소되었습니다.", response);
	}

}
