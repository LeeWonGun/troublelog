package com.min.edu.question.repository;

import com.min.edu.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * questions 테이블 조회를 담당하는 Repository이다.
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * 공개 질문 목록을 최신순으로 조회한다.
     *
     * PUBLIC 질문만 조회하고, 삭제된 질문은 제외한다.
     */
    List<Question> findByVisibilityAndDelflagOrderByCreatedAtDesc(String visibility, String delflag);

    /**
     * 질문 상세 조회 시 삭제되지 않은 질문만 조회한다.
     */
    Optional<Question> findByIdAndDelflag(Long id, String delflag);
}