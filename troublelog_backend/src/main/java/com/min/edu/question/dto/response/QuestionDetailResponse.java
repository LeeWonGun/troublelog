package com.min.edu.question.dto.response;

import com.min.edu.techstack.dto.response.TechStackResponse;

import java.time.LocalDateTime;
import java.util.List;

public record QuestionDetailResponse(
        Long questionId,
        String title,
        String content,
        String errorMessage,
        String environment,
        String tried,
        Long writerId,
        String writerNickname,

        // PUBLIC 질문이면 teamId는 null이다.
        Long teamId,

        // PUBLIC 질문이면 teamName은 null이다.
        String teamName,

        // UNSOLVED 또는 SOLVED
        String status,

        // PUBLIC 또는 TEAM
        String visibility,

        // 답변 수는 answers.depth = 0인 답변만 기준으로 계산한다.
        int answerCount,

        int likeCount,
        int viewCount,

        // 채택된 답변이 없으면 null이다.
        Long acceptedAnswerId,

        List<TechStackResponse> techStacks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}