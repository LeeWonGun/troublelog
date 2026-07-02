package com.min.edu.question.entity;

import com.min.edu.techstack.entity.TechStack;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 질문과 기술 스택의 연결 정보를 관리하는 Entity이다.
 */
/**
 * 질문과 기술 스택의 다대다 연결 정보를 관리하는 Entity입니다.
 */
@Entity
@Table(name = "question_tech_stacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionTechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 질문 ID이다.
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /**
     * 기술 스택 ID이다.
     */
    @Column(name = "tech_stack_id", nullable = false)
    private Long techStackId;

    /**
     * 응답 DTO 변환 시 기술 스택 이름과 카테고리를 조회하기 위한 읽기 전용 연관관계이다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_stack_id", insertable = false, updatable = false)
    private TechStack techStack;

    public QuestionTechStack(Long questionId, Long techStackId) {
        this.questionId = questionId;
        this.techStackId = techStackId;
    }
}
