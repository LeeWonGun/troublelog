package com.min.edu.answer;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.min.edu.answer.dto.AnswerCreateRequest;
import com.min.edu.answer.dto.AnswerResponse;
import com.min.edu.answer.dto.AnswerUpdateRequest;
import com.min.edu.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AnswerController {
	
	private final AnswerService answerService;
	
	// 답변 작성
	@PostMapping("/api/questions/{questionId}/answers")
	public ApiResponse<Long> createAnswer(
			@PathVariable Long questionId,
			@Valid @RequestBody AnswerCreateRequest request
	) {
		
		// TODO: JWT 인증 구현 후 현재 로그인 사용자 ID를 SecurityContext에서 가져오도록 수정
		Long writerId = 1L;
		
		Long answerId = answerService.createAnswer(questionId, writerId, request);
		
		return ApiResponse.success("답변이 작성되었습니다.", answerId);
		
	}
	
	
	// 댓글 작성
	@PostMapping("/api/answers/{answerId}/comments")
	public ApiResponse<Long> createComment(
			@PathVariable Long answerId,
			@Valid @RequestBody AnswerCreateRequest request
	) {
		// TODO: JWT 인증 구현 후 현재 로그인 사용자 ID를 SecurityContext에서 가져오도록 수정
		Long writerId = 1L;
		
		Long commentId = answerService.createComment(answerId, writerId, request);
		
		return ApiResponse.success("댓글이 작성되었습니다.", commentId);
		
	}
	
	
	// 대댓글 작성
	@PostMapping("/api/answers/{answerId}/replies")
	public ApiResponse<Long> createReply(
			@PathVariable Long answerId,
			@Valid @RequestBody AnswerCreateRequest request
	) {
		
		// TODO: JWT 인증 구현 후 현재 로그인 사용자 ID를 SecurityContext에서 가져오도록 수정
		Long writerId = 1L;
		
		Long replyId = answerService.createReply(answerId, writerId, request);
		
		return ApiResponse.success("대댓글이 작성되었습니다.", replyId);
	}
	
	
	
	// 답변/댓글/대댓글 계층 조회
	@GetMapping("/api/questions/{questionId}/answers")
	public ApiResponse<List<AnswerResponse>> getAnswers(@PathVariable Long questionId) {
		
		// TODO: JWT 인증 구현 후 현재 로그인 사용자 ID를 SecurityContext에서 가져오도록 수정
		Long userId = 1L;
		
		List<AnswerResponse> answers = answerService.getAnswers(questionId, userId);
		
		return ApiResponse.success("조회가 완료되었습니다.", answers);
		
	}
	
	
	// 답변/댓글/대댓글 수정
	@PutMapping("/api/answers/{answerId}")
	public ApiResponse<Void> updateAnswer(
			@PathVariable Long answerId,
			@Valid @RequestBody AnswerUpdateRequest request
	) {
		
		// TODO: JWT 인증 구현 후 현재 로그인 사용자 ID를 SecurityContext에서 가져오도록 수정
	    Long writerId = 1L;
	    
	    answerService.updateContent(answerId, writerId, request.getContent());
	    return ApiResponse.success("수정이 완료되었습니다.");
	}
	
	
	// 답변/댓글/대댓글 공통 삭제 (depth 상관없이 동일 엔드포인트)
	@DeleteMapping("/api/answers/{answerId}")
	public ApiResponse<Void> deleteAnswer(@PathVariable Long answerId) {
		
		// TODO: JWT 인증 구현 후 현재 로그인 사용자 ID를 SecurityContext에서 가져오도록 수정
	    Long writerId = 1L;
	    
	    answerService.deleteAnswer(answerId, writerId);
	    
	    return ApiResponse.success("삭제가 완료되었습니다.");
	}

}
