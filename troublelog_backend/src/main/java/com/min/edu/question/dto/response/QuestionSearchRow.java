package com.min.edu.question.dto.response;

import java.time.LocalDateTime;

/**
 * MyBatis 질문 검색 결과 한 행을 담는 조회 전용 DTO이다.
 *
 * 검색 API는 여러 테이블을 조합하거나 동적 조건을 적용할 수 있으므로,
 * JPA Entity 대신 조회 결과 전용 DTO로 받는다.
 */
public record QuestionSearchRow(
        Long questionId,
        String title,
        Long writerId,
        String writerNickname,
        String status,
        String visibility,
        int answerCount,
        int likeCount,
        int viewCount,
        LocalDateTime createdAt
) {
}