package com.min.edu.answer.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.min.edu.answer.AnswerEntity;
import com.min.edu.answer.dto.AnswerResponse;

@Mapper
public interface AnswerMapper {
	
	AnswerEntity findAnswerById(@Param("id") Long id);
	
	// 답변 작성: 질문 존재 여부와 visibility(공개/팀) 권한을 함께 검증한다.
	int insertAnswer(AnswerEntity answer);
	
	/* 
	 * 댓글/대댓글 작성: 부모의 depth를 그대로 +1 하여 저장하고,
	 * 부모 depth는 Service에서 이미 검증했으므로, SQL은 depth &lt; 2로 최종 방어만 한다.
	 */
	int insertCommentOrReply(AnswerEntity answer);
	
	
	// 질문이 존재하고, PUBLIC이거나 TEAM이면서 요청자가 팀원인 경우에만 1을 반환한다.
	int countAccessibleQuestion(@Param("questionId") Long questionId, @Param("userId") Long userId);
	
	// 질문에 달린 답변/댓글/대댓글을 평면 리스트로 가져온다. (트리 구성은 Service에서 처리)
	List<AnswerResponse> findAnswersByQuestionId(@Param("questionId") Long questionId);
	
	
	// 답변/댓글/대댓글 수정: 작성자 본인이고, 삭제되지 않았고, TEAM이면 현재도 팀원인 경우에만 허용한다.
	int updateContent(@Param("id") Long id, @Param("writerId") Long writerId, @Param("content") String content);
	
	// questions.accepted_answer_id가 이 답변 id와 일치하는지 확인한다.
	boolean isAcceptedAnswer(@Param("answerId") Long answerId);
}