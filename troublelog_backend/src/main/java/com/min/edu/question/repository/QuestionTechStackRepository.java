package com.min.edu.question.repository;

import com.min.edu.question.entity.QuestionTechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 질문과 기술 스택의 연결 정보를 조회/저장하는 Repository입니다.
 */
public interface QuestionTechStackRepository extends JpaRepository<QuestionTechStack, Long> {

    @Query("""
            select qts
            from QuestionTechStack qts
            join fetch qts.techStack
            where qts.questionId = :questionId
            """)
    List<QuestionTechStack> findByQuestionIdWithTechStack(@Param("questionId") Long questionId);

    @Query("""
            select qts
            from QuestionTechStack qts
            join fetch qts.techStack
            where qts.questionId in :questionIds
            """)
    List<QuestionTechStack> findByQuestionIdInWithTechStack(@Param("questionIds") List<Long> questionIds);

    @Modifying(flushAutomatically = true)
    @Query("delete from QuestionTechStack qts where qts.questionId = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);
}
