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
	
	
	// 답변/댓글/대댓글 수정
	@Transactional
	public void updateContent(Long id, Long writerId, String content) {
		
		AnswerEntity answer = answerMapper.findAnswerById(id);
		
		if(answer == null) {
			throw new BusinessException("답변을 찾을 수 없습니다", ErrorCode.ANSWER_NOT_FOUND);
		}
		
		if(!answer.getWriterId().equals(writerId)) {
			throw new BusinessException("작성자만 수정할 수 있습니다", ErrorCode.FORBIDDEN);
		}
		
		// 내용이 바뀌지 않았다면 불필요한 갱신(updated_at 변경)을 막기 위해 그대로 종료한다.
		if(content.equals(answer.getContent())) {
			return;
		}
		
		/*
		 * 최상위 답변(depth=0)까지 거슬러 올라가서, 그 답변이 채택되었는지 확인한다.
		 * 채택된 답변뿐 아니라 그 아래 댓글/대댓글도 함께 수정을 제한한다.
		 */
		Long rootAnswerId = findRootAnswerId(answer);
		boolean isAccepted = answerMapper.isAcceptedAnswer(rootAnswerId);
		
		if(isAccepted) {
			throw new BusinessException("채택된 답변과 그에 달린 댓글/대댓글은 수정할 수 없습니다", ErrorCode.ACCEPTED_ANSWER_CANNOT_DELETE);
		}
		
		int updated = answerMapper.updateContent(id, writerId, content);
		
		if(updated == 0) {
			/* 
			 * 작성자 확인을 이미 통과했는데도 0건이면 TEAM 질문에서 팀 탈퇴로 권한을 잃었거나, 
			 * 동시 요청으로 상태가 바뀐 경우
			 */
			throw new BusinessException("내용을 수정할 수 없습니다", ErrorCode.ANSWER_NOT_FOUND);
		}
	}
	

	/* 
	 * depth=0(최상위 답변)에 도달할 때까지 부모를 따라 올라간다.
	 * 부모 삭제 시 자식도 함께 삭제되는 정책이므로, 자식이 살아있다면 부모도 항상 살아있다. 
	 */
	private Long findRootAnswerId(AnswerEntity answer) {
		
		if(answer.getDepth() == 0) {
			return answer.getId();
		}
		
		AnswerEntity parent = answerMapper.findAnswerById(answer.getParentAnswerId());
		
		if(parent == null) {
			// 정상적으로는 발생할 수 없는 상태 (연쇄 삭제 정책 위반 시에만 발생)
			throw new BusinessException("답변 구조에 문제가 있습니다", ErrorCode.ANSWER_NOT_FOUND);
		}
		
		return findRootAnswerId(parent);
	}
	
	
	// 답변/댓글/대댓글 삭제 (soft delete). 하위 댓글/대댓글까지 cascade 삭제한다.
	@Transactional
	public void deleteAnswer(Long id, Long writerId) {
		
		AnswerEntity answer = answerMapper.findAnswerById(id);
		
		if(answer == null) {
			throw new BusinessException("답변을 찾을 수 없습니다", ErrorCode.ANSWER_NOT_FOUND);
		}
		
		if(!answer.getWriterId().equals(writerId)) {
			throw new BusinessException("작성자만 삭제할 수 있습니다", ErrorCode.FORBIDDEN);
		}
		
		/*
		 * 최상위 답변(depth=0)까지 거슬러 올라가서, 그 답변이 채택되었는지 확인한다.
		 * 채택된 답변뿐 아니라 그 아래 댓글/대댓글도 함께 삭제를 제한한다.
		 * (updateContent와 동일한 findRootAnswerId 재사용)
		 */
		Long rootAnswerId = findRootAnswerId(answer);
		boolean isAccepted = answerMapper.isAcceptedAnswer(rootAnswerId);
		
		if(isAccepted) {
			throw new BusinessException("채택된 답변과 그에 달린 댓글/대댓글은 삭제할 수 없습니다", ErrorCode.ACCEPTED_ANSWER_CANNOT_DELETE);
		}
		
		int deleted = answerMapper.softDeleteAnswer(id, writerId);
		
		if(deleted == 0) {
			/* 
			 * 작성자 확인을 이미 통과했는데도 0건이면 TEAM 질문에서 팀 탈퇴로 권한을 잃었거나, 
			 * 동시 요청으로 상태가 바뀐 경우 (updateContent와 동일하게 처리)
			 */
			throw new BusinessException("삭제할 수 없습니다", ErrorCode.ANSWER_NOT_FOUND);
		}
		
		// 하위 댓글/대댓글도 함께 소프트 삭제 (작성자 무관 cascade)
		answerMapper.softDeleteDescendants(id);
		
	}
	
	
	/* 
	 * 답변 채택/해제 토글 (질문 작성자 본인만, depth=0 답변만 채택 가능)
	 * URL에 answerId만 오므로, 소속 questionId는 답변을 통해 조회한다. 
	 */
	@Transactional
	public void toggleAcceptAnswer(Long answerId, Long userId) {
		
		AnswerEntity answer = answerMapper.findAnswerById(answerId);
		
		if(answer == null) {
			throw new BusinessException("답변을 찾을 수 없습니다", ErrorCode.ANSWER_NOT_FOUND);
		}
		
		if(answer.getDepth() != 0) {
			throw new BusinessException("답변만 채택할 수 있습니다", ErrorCode.ONLY_DEPTH_ZERO_CAN_BE_ACCEPTED);
		}
		
		Long questionId = answer.getQuestionId();
		
		/* 
		 * 토글 방향 판단용 조회일 뿐, 여기서 나온 null은 권한 판단 근거로 쓰지 않는다.
		 * 실제 권한 검증은 아래 acceptAnswer/cancelAcceptedAnswer의 WHERE절(작성자 일치)이 담당한다. 
		 */
		Long currentAcceptedId = answerMapper.findAcceptedAnswerId(questionId, userId);
		
		if(answerId.equals(currentAcceptedId)) {
			// 이미 채택된 답변을 다시 누른 경우 -> 해제
			int canceled = answerMapper.cancelAcceptedAnswer(questionId, userId);
			if(canceled == 0) {
				throw new BusinessException("질문 작성자만 채택을 해제할 수 있습니다", ErrorCode.FORBIDDEN);
			}
		} else {
			// 새로 채택하거나, 다른 답변으로 교체
			int accepted = answerMapper.acceptAnswer(questionId, answerId, userId);
			if(accepted == 0) {
				throw new BusinessException("질문 작성자만 답변을 채택할 수 있습니다", ErrorCode.FORBIDDEN);
			}
		}
	}
	
	
	
	
	
	
}











