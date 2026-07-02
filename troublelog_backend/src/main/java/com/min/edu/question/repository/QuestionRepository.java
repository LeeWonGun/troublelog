package com.min.edu.question.repository;

import com.min.edu.question.entity.Question;
import com.min.edu.question.entity.QuestionStatus;
import com.min.edu.question.entity.QuestionVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * questions 테이블의 기본 Entity 조회를 담당하는 Repository이다.
 *
 * 단순 조회, 상세 조회, 저장/수정/삭제처럼 Entity 상태 변경이 필요한 작업은 JPA로 처리한다.
 * 조건이 많은 검색 쿼리는 MyBatis Mapper에서 처리한다.
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {

    Page<Question> findByVisibilityAndDelflag(
            QuestionVisibility visibility,
            String delflag,
            Pageable pageable
    );

    Page<Question> findByVisibilityAndDelflagAndStatus(
            QuestionVisibility visibility,
            String delflag,
            QuestionStatus status,
            Pageable pageable
    );

    Page<Question> findByWriterIdAndDelflag(
            Long writerId,
            String delflag,
            Pageable pageable
    );

    Page<Question> findByWriterIdAndDelflagAndStatus(
            Long writerId,
            String delflag,
            QuestionStatus status,
            Pageable pageable
    );

    Page<Question> findByTeamIdAndVisibilityAndDelflag(
            Long teamId,
            QuestionVisibility visibility,
            String delflag,
            Pageable pageable
    );

    Page<Question> findByTeamIdAndVisibilityAndDelflagAndStatus(
            Long teamId,
            QuestionVisibility visibility,
            String delflag,
            QuestionStatus status,
            Pageable pageable
    );

    Optional<Question> findByIdAndDelflag(Long id, String delflag);

    @Modifying
    @Query("update Question q set q.viewCount = q.viewCount + 1 where q.id = :questionId and q.delflag = 'N'")
    int increaseViewCount(@Param("questionId") Long questionId);
}
