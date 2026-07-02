package com.min.edu.like.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper {

	
	/*
     * 질문 좋아요 대상 접근 가능 여부를 확인한다.
     * PUBLIC 질문이거나, TEAM 질문이면 요청자가 활성 팀원인지 확인한다.
     */
	int existsAccessibleQuestion(@Param("questionId") Long questionId, @Param("userId") Long userId);
	
	
	/*
     * 답변 좋아요 대상 접근 가능 여부를 확인한다.
     * depth=0인 답변만 허용하며, 소속 질문 기준으로 PUBLIC/TEAM 접근 권한을 판단한다.
     */
	int existsAccessibleAnswer(@Param("answerId") Long answerId, @Param("userId") Long userId);
	
	
	/*
     * 중복 좋아요 방지 INSERT (NOT EXISTS 조건부 INSERT).
     * 영향받은 row 수 반환 (0이면 이미 존재해서 삽입 안 됨 = 레이스 컨디션 방어)
     */
	int insertIfNotExists(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") String targetType);
	
	
	// likes 테이블 기준으로 질문의 like_count를 다시 계산해 저장한다.
	void updateQuestionCount(@Param("questionId") Long questionId);
	
	
	// likes 테이블 기준으로 답변의 like_count를 다시 계산해 저장한다.
	void updateAnswerCount(@Param("answerId") Long answerId);
	
	
	// 질문 좋아요 수 조회
	int selectQuestionLikeCount(@Param("questionId") Long questionId);
	
	
	// 답변 좋아요 수 조회
	int selectAnswerLikeCount(@Param("answerId") Long answerId);
	
	
}
