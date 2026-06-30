package com.min.edu.answer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.min.edu.answer.dto.AnswerCreateRequest;
import com.min.edu.answer.dto.AnswerResponse;
import com.min.edu.answer.repository.AnswerMapper;
import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AnswerService {
	
	private final AnswerMapper answerMapper;

	// 답변 작성 (depth = 0)
	@Transactional
	public Long createAnswer(Long questionId, Long writerId, AnswerCreateRequest request) {
		
		AnswerEntity answer = AnswerEntity.builder()
				.questionId(questionId)
				.writerId(writerId)
				.content(request.getContent())
				.build();
		
		int inserted = answerMapper.insertAnswer(answer);
		
		if(inserted == 0) {
			throw new BusinessException("질문을 찾을 수 없거나 접근 권한이 없습니다", ErrorCode.QUESTION_NOT_FOUND);
		}
		
		return answer.getId();
		
	}
	
	

}
