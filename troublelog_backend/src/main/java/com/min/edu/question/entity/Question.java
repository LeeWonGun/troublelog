package com.min.edu.question.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 질문 게시글 정보를 관리하는 Entity이다.
 *
 * 회원/팀 Entity는 아직 인증 담당 PR과 충돌 가능성이 있으므로,
 * 현재 단계에서는 writerId, teamId를 Long 값으로만 관리한다.
 */
@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자 ID이다. User Entity 연동은 인증 구조 확정 후 처리한다.
    @Column(name = "writer_id", nullable = false)
    private Long writerId;

    // PUBLIC 질문이면 null, TEAM 질문이면 팀 ID가 저장된다.
    @Column(name = "team_id")
    private Long teamId;

    @Column(nullable = false)
    private String title;

    // 상황 설명과 Markdown 코드블록을 함께 저장한다.
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String environment;

    @Column(columnDefinition = "TEXT")
    private String tried;

    // PUBLIC 또는 TEAM
    @Column(nullable = false)
    private String visibility;

    // UNSOLVED 또는 SOLVED
    @Column(nullable = false)
    private String status;

    @Column(name = "answer_count", nullable = false)
    private int answerCount;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    // 채택된 답변이 없으면 null이다.
    @Column(name = "accepted_answer_id")
    private Long acceptedAnswerId;

    // N: 정상, Y: 삭제
    @Column(nullable = false, columnDefinition = "CHAR(1)")
    private String delflag;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 질문 상세 조회 시 조회수를 1 증가시킨다.
     */
    public void increaseViewCount() {
        this.viewCount++;
    }
}
