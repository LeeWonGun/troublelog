package com.min.edu.answer.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.min.edu.answer.AnswerEntity;

@Mapper
public interface AnswerMapper {
	
	// 답변 작성: 질문 존재 여부와 visibility(공개/팀) 권한을 함께 검증한다.
	int insertAnswer(AnswerEntity answer);
	
}