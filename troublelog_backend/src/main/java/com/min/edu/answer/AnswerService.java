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
	
	
	// 댓글 작성 (depth = 1, 부모는 반드시 답변이어야 한다)
	@Transactional
	public Long createComment(Long parentAnswerId, Long writerId, AnswerCreateRequest request) {
		
		AnswerEntity parent = answerMapper.findAnswerById(parentAnswerId);
		
		if(parent == null) {
			throw new BusinessException("답변을 찾을 수 없습니다", ErrorCode.ANSWER_NOT_FOUND);
		}
		
		if(parent.getDepth() != 0) {
			throw new BusinessException("답변에만 댓글을 작성할 수 있습니다", ErrorCode.ANSWER_NOT_FOUND);
		}
		
		AnswerEntity comment = AnswerEntity.builder()
				.parentAnswerId(parentAnswerId)
				.writerId(writerId)
				.content(request.getContent())
				.build();
		
		int inserted = answerMapper.insertCommentOrReply(comment);
		
		if(inserted == 0) {
			// 부모가 조회 이후 동시에 삭제된 경우 (동시성 최종 방어선)
			throw new BusinessException("답변을 찾을 수 없습니다", ErrorCode.ANSWER_NOT_FOUND);
		}
		
		return comment.getId();
	}
	
	
	// 대댓글 작성 (depth = 2, 부모는 반드시 댓글이어야 한다)
	@Transactional
	public Long createReply(Long parentAnswerId, Long writerId, AnswerCreateRequest request) {
		
		AnswerEntity parent = answerMapper.findAnswerById(parentAnswerId);
		
		if(parent == null) {
			throw new BusinessException("댓글을 찾을 수 없습니다", ErrorCode.ANSWER_NOT_FOUND);
		}
		
		if(parent.getDepth() != 1) {
			throw new BusinessException("댓글에만 대댓글을 작성할 수 있습니다", ErrorCode.ANSWER_NOT_FOUND);
		}
		
		AnswerEntity reply = AnswerEntity.builder()
				.parentAnswerId(parentAnswerId)
				.writerId(writerId)
				.content(request.getContent())
				.build();
		
		int inserted = answerMapper.insertCommentOrReply(reply);
		
		if(inserted == 0) {
			throw new BusinessException("댓글을 찾을 수 없습니다", ErrorCode.ANSWER_NOT_FOUND);
		}
		
		return reply.getId();
	}
	
	
	// 답변/댓글/대댓글 계층 조회
	public List<AnswerResponse> getAnswers(Long questionId, Long userId) {
		
		int accessible = answerMapper.countAccessibleQuestion(questionId, userId);
		
		if(accessible == 0) {
			throw new BusinessException("질문을 찾을 수 없거나 접근 권한이 없습니다", ErrorCode.QUESTION_NOT_FOUND);
		}
		
		List<AnswerResponse> all = answerMapper.findAnswersByQuestionId(questionId);
		
		// parentAnswerId 기준으로 자식들을 미리 그룹핑해둔다.
		Map<Long, List<AnswerResponse>> childrenByParent = all.stream()
				.filter(a -> a.getParentAnswerId() != null)
				.collect(Collectors.groupingBy(AnswerResponse::getParentAnswerId));
		
		// depth = 0(답변)만 최상위로 두고, 나머지는 재귀적으로 children에 붙인다.
		return all.stream()
				.filter(a -> a.getDepth() == 0)
				.map(answer -> attachChildren(answer, childrenByParent))
				.collect(Collectors.toList());
		
	}
	
	
	/* 
	 * 부모-자식 관계를 재귀적으로 타고 내려가며 children을 채운다. 
	 * depth는 0~2로 제한되어 있지만, 트리 구성 로직 자체는 깊이에 의존하지 않는 범용 방식으로 작성했다. 
	 */
	private AnswerResponse attachChildren(AnswerResponse answer, Map<Long, List<AnswerResponse>> childrenByParent) {
		
		List<AnswerResponse> children = childrenByParent.getOrDefault(answer.getId(), List.of())
				.stream()
				.map(child -> attachChildren(child, childrenByParent)) // 형제들 하나씩 다 처리
				.collect(Collectors.toList());
		
		answer.setChildren(children);
		
		return answer;
	}

}
