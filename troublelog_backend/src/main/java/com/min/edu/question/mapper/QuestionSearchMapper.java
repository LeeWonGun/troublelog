package com.min.edu.question.mapper;

import com.min.edu.question.dto.response.QuestionSearchRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 질문 검색처럼 조건이 많은 조회 쿼리를 담당하는 MyBatis Mapper이다.
 *
 * keyword, status, techStackIds, sort처럼 동적으로 조합되는 검색 조건은
 * JPA Repository보다 MyBatis XML에서 관리하는 것이 더 명확하다.
 */
@Mapper
public interface QuestionSearchMapper {

    /**
     * 공개 질문을 검색 조건에 맞게 조회한다.
     *
     * keyword는 제목 또는 본문 검색에 사용하고,
     * status는 해결/미해결 필터에 사용한다.
     * techStackIds가 있으면 해당 기술 스택 중 하나라도 연결된 질문만 조회한다.
     * sort는 latest 또는 popular 값을 사용한다.
     */
    List<QuestionSearchRow> searchPublicQuestions(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("techStackIds") List<Long> techStackIds,
            @Param("sort") String sort
    );
}