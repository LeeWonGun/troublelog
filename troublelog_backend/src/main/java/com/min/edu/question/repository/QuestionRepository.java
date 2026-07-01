package com.min.edu.question.repository;

import com.min.edu.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * questions 테이블의 기본 Entity 조회를 담당하는 Repository이다.
 *
 * 단순 조회, 상세 조회, 저장/수정/삭제처럼 Entity 상태 변경이 필요한 작업은 JPA로 처리한다.
 * 조건이 많은 검색 쿼리는 MyBatis Mapper에서 처리한다.
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * 공개 질문 목록을 페이징 조회한다.
     *
     * PUBLIC 질문만 조회하고, 삭제된 질문은 제외한다.
     */
    Page<Question> findByVisibilityAndDelflag(
            String visibility,
            String delflag,
            Pageable pageable
    );

    /**
     * 공개 질문 목록을 상태 조건까지 포함하여 페이징 조회한다.
     *
     * sort=SOLVED 또는 sort=UNSOLVED 요청을 처리할 때 사용한다.
     */
    Page<Question> findByVisibilityAndDelflagAndStatus(
            String visibility,
            String delflag,
            String status,
            Pageable pageable
    );

    /**
     * 질문 상세 조회 시 삭제되지 않은 질문만 조회한다.
     */
    Optional<Question> findByIdAndDelflag(Long id, String delflag);
}