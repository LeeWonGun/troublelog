package com.min.edu.like.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "user_id", nullable = false)
	private Long userId;
	
	// 좋아요 대상 ID이다. target_type에 따라 questions.id 또는 answers.id를 의미한다.
	@Column(name = "target_id", nullable = false)
	private Long targetId;
	
	// 좋아요 대상 구분이다. QUE: 질문, ANS: 답변(depth=0만 해당).
	@Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private TargetType targetType;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@Builder
	public LikeEntity(Long userId, Long targetId, TargetType targetType) {
		this.userId = userId;
		this.targetId = targetId;
		this.targetType = targetType;
		this.createdAt = LocalDateTime.now();
	}
	
	
	public enum TargetType {
        QUE, ANS
    }

}
