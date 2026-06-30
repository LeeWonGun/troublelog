package com.min.edu.question.dto.response;

import com.min.edu.techstack.dto.response.TechStackResponse;

import java.time.LocalDateTime;
import java.util.List;

public record QuestionListResponse(
        Long questionId,
        String title,
        Long writerId,
        String writerNickname,

        // UNSOLVED 또는 SOLVED
        String status,

        // PUBLIC 또는 TEAM
        String visibility,

        // 답변 수는 answers.depth = 0인 답변만 기준으로 계산한다.
        int answerCount,

        int likeCount,
        int viewCount,
        List<TechStackResponse> techStacks,
        LocalDateTime createdAt
) {
}