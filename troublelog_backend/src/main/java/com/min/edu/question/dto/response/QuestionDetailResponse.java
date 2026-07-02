package com.min.edu.question.dto.response;

import com.min.edu.question.entity.Question;
import com.min.edu.question.util.QuestionContentParts;
import com.min.edu.techstack.dto.response.TechStackResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 질문 상세 화면에 필요한 본문, 작성자, 팀, 좋아요, 기술 스택 정보를 담는 응답 DTO입니다.
 */
public record QuestionDetailResponse(
        Long questionId,
        String title,
        String content,
        String codeLanguage,
        String code,
        String errorMessage,
        String environment,
        String tried,
        Long writerId,
        String writerNickname,
        Long teamId,
        String teamName,
        String status,
        String visibility,
        int answerCount,
        int likeCount,
        boolean likedByMe,
        int viewCount,
        Long acceptedAnswerId,
        List<TechStackResponse> techStacks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static QuestionDetailResponse from(
            Question question,
            QuestionContentParts contentParts,
            List<TechStackResponse> techStacks,
            String writerNickname,
            String teamName,
            boolean likedByMe
    ) {
        return new QuestionDetailResponse(
                question.getId(),
                question.getTitle(),
                contentParts.content(),
                contentParts.codeLanguage(),
                contentParts.code(),
                question.getErrorMessage(),
                question.getEnvironment(),
                question.getTried(),
                question.getWriterId(),
                writerNickname,
                question.getTeamId(),
                teamName,
                question.getStatus().name(),
                question.getVisibility().name(),
                question.getAnswerCount(),
                question.getLikeCount(),
                likedByMe,
                question.getViewCount(),
                question.getAcceptedAnswerId(),
                techStacks,
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
