package com.min.edu.answer.repository;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.min.edu.answer.AnswerEntity;

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
	
}