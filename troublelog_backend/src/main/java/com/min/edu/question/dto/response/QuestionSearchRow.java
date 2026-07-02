package com.min.edu.question.dto.response;

import java.time.LocalDateTime;

/**
 * MyBatis 질문 검색 결과의 한 행을 담는 조회 전용 DTO입니다.
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
