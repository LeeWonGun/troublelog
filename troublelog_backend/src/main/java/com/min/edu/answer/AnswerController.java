package com.min.edu.answer;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.min.edu.answer.dto.AnswerCreateRequest;
import com.min.edu.answer.dto.AnswerResponse;
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
	
	
}
