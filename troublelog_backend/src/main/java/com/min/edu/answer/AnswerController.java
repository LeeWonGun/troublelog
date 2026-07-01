package com.min.edu.answer;

import java.util.List;

import org.springframework.security.core.Authentication;
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
import com.min.edu.auth.security.CurrentUser;
import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
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
			@Valid @RequestBody AnswerCreateRequest request,
			Authentication authentication
	) {
		
		Long writerId = CurrentUser.id(authentication);
		Long answerId = answerService.createAnswer(questionId, writerId, request);
		
		return ApiResponse.success("답변이 작성되었습니다.", answerId);
		
	}
	
	
	// 댓글 작성
	@PostMapping("/api/answers/{answerId}/comments")
	public ApiResponse<Long> createComment(
			@PathVariable Long answerId,
			@Valid @RequestBody AnswerCreateRequest request,
			Authentication authentication
	) {
		
		Long writerId = CurrentUser.id(authentication);
		Long commentId = answerService.createComment(answerId, writerId, request);
		
		return ApiResponse.success("댓글이 작성되었습니다.", commentId);
		
	}
	
	
	// 대댓글 작성
	@PostMapping("/api/answers/{answerId}/replies")
	public ApiResponse<Long> createReply(
			@PathVariable Long answerId,
			@Valid @RequestBody AnswerCreateRequest request,
			Authentication authentication
	) {
		
		Long writerId = CurrentUser.id(authentication);
		Long replyId = answerService.createReply(answerId, writerId, request);
		
		return ApiResponse.success("대댓글이 작성되었습니다.", replyId);
	}
	
	
	
	// 답변/댓글/대댓글 계층 조회
	@GetMapping("/api/questions/{questionId}/answers")
	public ApiResponse<List<AnswerResponse>> getAnswers(
			@PathVariable Long questionId,
			Authentication authentication
	) {
		
		/* 
		 * 비회원 조회 허용: 로그인 안 한 사용자는 CurrentUser.id()가 UNAUTHORIZED를 던지므로 잡아서 null 처리한다.
		 * null이어도 하위 SQL이 PUBLIC은 통과, TEAM은 자연히 차단하도록 짜여 있어 안전하다.
		 */
		Long userId;
		
		try {
		    userId = CurrentUser.id(authentication);
		} catch (BusinessException e) {
			
			// 로그인 안 한 경우가 아니라면 원래 예외 그대로 던진다
			if (e.getErrorCode() != ErrorCode.UNAUTHORIZED) {
		        throw e;
		    }
		    userId = null;
		}
		
		List<AnswerResponse> answers = answerService.getAnswers(questionId, userId);
		
		return ApiResponse.success("조회가 완료되었습니다.", answers);
		
	}
	
	
	// 답변/댓글/대댓글 수정
	@PutMapping("/api/answers/{answerId}")
	public ApiResponse<Void> updateAnswer(
			@PathVariable Long answerId,
			@Valid @RequestBody AnswerUpdateRequest request,
			Authentication authentication
	) {
		
		Long writerId = CurrentUser.id(authentication);
	    
	    answerService.updateContent(answerId, writerId, request.getContent());
	    return ApiResponse.success("수정이 완료되었습니다.");
	}
	
	
	// 답변/댓글/대댓글 공통 삭제 (depth 상관없이 동일 엔드포인트)
	@DeleteMapping("/api/answers/{answerId}")
	public ApiResponse<Void> deleteAnswer(
			@PathVariable Long answerId, 
			Authentication authentication
	) {
		
		Long writerId = CurrentUser.id(authentication);
	    
	    answerService.deleteAnswer(answerId, writerId);
	    
	    return ApiResponse.success("삭제가 완료되었습니다.");
	}
	
	
	// 답변 채택/해제 토글
	@PostMapping("/api/answers/{answerId}/accept")
	public ApiResponse<Void> toggleAcceptAnswer(
			@PathVariable Long answerId,
			Authentication authentication
	) {
		
		Long userId = CurrentUser.id(authentication);

		answerService.toggleAcceptAnswer(answerId, userId);

		return ApiResponse.success("채택 상태가 변경되었습니다.");
	}

}
