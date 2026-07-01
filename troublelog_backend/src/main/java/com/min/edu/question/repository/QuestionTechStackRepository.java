package com.min.edu.question.repository;

import com.min.edu.question.entity.QuestionTechStack;
import com.min.edu.question.entity.QuestionTechStackId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 질문과 기술 스택 연결 정보를 조회/저장하는 Repository이다.
 */
public interface QuestionTechStackRepository extends JpaRepository<QuestionTechStack, QuestionTechStackId> {

    List<QuestionTechStack> findByQuestionId(Long questionId);

    List<QuestionTechStack> findByQuestionIdIn(List<Long> questionIds);

    void deleteByQuestionId(Long questionId);
}