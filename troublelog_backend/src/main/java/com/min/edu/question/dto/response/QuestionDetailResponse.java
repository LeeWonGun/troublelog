package com.min.edu.question.dto.response;

import com.min.edu.question.entity.Question;
import com.min.edu.question.util.QuestionContentParts;
import com.min.edu.techstack.dto.response.TechStackResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 질문 상세 화면의 질문 본문 영역을 표현하는 응답 DTO이다.
 */
public record QuestionDetailResponse(
        Long questionId,
        String title,

        // questions.content에 저장된 Markdown 문자열에서 상황 설명만 분리한 값이다.
        String content,

        // Markdown 코드블록의 언어 태그 값이다. 코드가 없으면 null이다.
        String codeLanguage,

        // Markdown 코드블록 안의 코드 내용이다. 코드가 없으면 null이다.
        String code,

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

    /**
     * Question Entity와 content 분리 결과, 기술 스택 목록을 질문 상세 응답 DTO로 변환한다.
     *
     * writerNickname, teamName은 회원/팀 구조가 확정된 뒤 연결한다.
     */
    public static QuestionDetailResponse from(
            Question question,
            QuestionContentParts contentParts,
            List<TechStackResponse> techStacks
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
                null,
                question.getTeamId(),
                null,
                question.getStatus().name(),
                question.getVisibility().name(),
                question.getAnswerCount(),
                question.getLikeCount(),
                question.getViewCount(),
                question.getAcceptedAnswerId(),
                techStacks,
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }

    public static QuestionDetailResponse from(
            Question question,
            QuestionContentParts contentParts,
            List<TechStackResponse> techStacks,
            String writerNickname,
            String teamName
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
                question.getViewCount(),
                question.getAcceptedAnswerId(),
                techStacks,
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
