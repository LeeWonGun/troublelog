package com.min.edu.question.mapper;

import com.min.edu.question.dto.response.QuestionSearchRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 질문 검색처럼 조건이 많은 조회 쿼리를 담당하는 MyBatis Mapper이다.
 *
 * keyword, status, techStackIds, sort, teamId처럼 동적으로 조합되는 검색 조건은
 * JPA Repository보다 MyBatis XML에서 관리하는 것이 더 명확하다.
 */
/**
 * 질문 검색처럼 동적 조건이 많은 조회 쿼리를 담당하는 MyBatis Mapper입니다.
 */
@Mapper
public interface QuestionSearchMapper {

    /**
     * 공개 또는 팀 질문을 검색 조건에 맞게 페이징 조회한다.
     *
     * teamId가 null이면 전체 공개 게시판 검색,
     * teamId가 있으면 해당 팀 질문 검색으로 사용한다.
     */
    List<QuestionSearchRow> searchQuestions(
            @Param("teamId") Long teamId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("techStackIds") List<Long> techStackIds,
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("size") int size
    );

    /**
     * 검색 조건에 맞는 전체 질문 수를 조회한다.
     *
     * 페이징 응답의 totalElements 계산에 사용한다.
     */
    long countSearchQuestions(
            @Param("teamId") Long teamId,
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("techStackIds") List<Long> techStackIds
    );
}
