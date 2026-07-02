package com.min.edu.question.entity;

import com.min.edu.techstack.entity.TechStack;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 질문과 기술 스택의 연결 정보를 관리하는 Entity이다.
 *
 * question_tech_stacks 테이블은 별도의 id 컬럼이 없는 연결 테이블이므로,
 * question_id와 tech_stack_id 조합을 복합키로 사용한다.
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
     *
     * 복합키에 포함된다.
     * Question Entity와의 직접 연관관계는 인증/질문 작성 구조가 더 확정된 뒤 검토한다.
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /**
     * 기술 스택 ID이다.
     *
     * 복합키에 포함된다.
     */
    @Column(name = "tech_stack_id", nullable = false)
    private Long techStackId;

    /**
     * 질문 목록/상세 응답에서 기술 스택 이름과 카테고리가 필요하므로
     * TechStack Entity와 조회용 연관관계를 맺는다.
     *
     * tech_stack_id 컬럼은 위의 techStackId 필드가 관리하므로,
     * 이 연관관계에서는 insert/update를 하지 않도록 설정한다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_stack_id", insertable = false, updatable = false)
    private TechStack techStack;

    public QuestionTechStack(Long questionId, Long techStackId) {
        this.questionId = questionId;
        this.techStackId = techStackId;
    }
}
