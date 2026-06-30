package com.min.edu.question.repository;

import com.min.edu.question.entity.QuestionTechStack;
import com.min.edu.question.entity.QuestionTechStackId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 질문과 기술 스택 연결 정보를 조회하는 Repository이다.
 */
public interface QuestionTechStackRepository extends JpaRepository<QuestionTechStack, QuestionTechStackId> {

    /**
     * 질문 상세 조회에서 특정 질문에 연결된 기술 스택 목록을 조회한다.
     */
    List<QuestionTechStack> findByQuestionId(Long questionId);

    /**
     * 질문 목록 조회에서 여러 질문에 연결된 기술 스택 목록을 한 번에 조회한다.
     */
    List<QuestionTechStack> findByQuestionIdIn(List<Long> questionIds);
}