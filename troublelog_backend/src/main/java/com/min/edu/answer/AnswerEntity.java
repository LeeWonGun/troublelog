package com.min.edu.answer;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "answers")
@Getter
@Setter // MyBatis useGeneratedKeys로 INSERT 후 id를 채워 넣기 위해 필요
@NoArgsConstructor
public class AnswerEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "question_id", nullable = false)
	private Long questionId;

	@Column(name = "writer_id", nullable = false)
	private Long writerId;
	
	@Column(name = "parent_answer_id")
    private Long parentAnswerId;
	
	// 답변: 0, 댓글: 1, 대댓글: 2
	@Column(nullable = false, columnDefinition = "TINYINT")
	private int depth;
	
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;
	
	@Column(name = "like_count", nullable = false)
	private int likeCount;
	
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private DelFlag delflag;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Builder
    private AnswerEntity(Long questionId, Long writerId, Long parentAnswerId, int depth, String content) {
        this.questionId = questionId;
        this.writerId = writerId;
        this.parentAnswerId = parentAnswerId;
        this.depth = depth;
        this.content = content;
        this.likeCount = 0;
        this.delflag = DelFlag.N;
        this.createdAt = LocalDateTime.now();
    }
    
    public enum DelFlag {
        N, Y
    }
    
    
}
