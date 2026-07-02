package com.min.edu.question.dto.response;

import com.min.edu.question.entity.Question;
import com.min.edu.techstack.dto.response.TechStackResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 질문 목록 화면에서 질문 카드 하나를 표현하는 응답 DTO이다.
 */
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

    /**
     * Question Entity와 기술 스택 목록을 질문 목록 응답 DTO로 변환한다.
     *
     * writerNickname은 인증/회원 구조가 확정된 뒤 User 정보와 연결해서 채운다.
     */
    public static QuestionListResponse from(
            Question question,
            List<TechStackResponse> techStacks
    ) {
        return new QuestionListResponse(
                question.getId(),
                question.getTitle(),
                question.getWriterId(),
                null,
                question.getStatus().name(),
                question.getVisibility().name(),
                question.getAnswerCount(),
                question.getLikeCount(),
                question.getViewCount(),
                techStacks,
                question.getCreatedAt()
        );
    }

    public static QuestionListResponse from(
            Question question,
            String writerNickname,
            List<TechStackResponse> techStacks
    ) {
        return new QuestionListResponse(
                question.getId(),
                question.getTitle(),
                question.getWriterId(),
                writerNickname,
                question.getStatus().name(),
                question.getVisibility().name(),
                question.getAnswerCount(),
                question.getLikeCount(),
                question.getViewCount(),
                techStacks,
                question.getCreatedAt()
        );
    }

    /**
     * MyBatis 검색 결과 Row와 기술 스택 목록을 질문 목록 응답 DTO로 변환한다.
     */
    public static QuestionListResponse fromSearchRow(
            QuestionSearchRow row,
            List<TechStackResponse> techStacks
    ) {
        return new QuestionListResponse(
                row.questionId(),
                row.title(),
                row.writerId(),
                row.writerNickname(),
                row.status(),
                row.visibility(),
                row.answerCount(),
                row.likeCount(),
                row.viewCount(),
                techStacks,
                row.createdAt()
        );
    }
}
