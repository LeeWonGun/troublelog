package com.min.edu.question.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * question_tech_stacks 테이블의 복합키를 표현하는 ID 클래스이다.
 *
 * 해당 테이블은 별도의 id 컬럼을 두지 않고,
 * question_id와 tech_stack_id 조합으로 질문과 기술 스택의 연결을 식별한다.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class QuestionTechStackId implements Serializable {

    /**
     * QuestionTechStack Entity의 @Id 필드명과 반드시 같아야 한다.
     */
    private Long questionId;

    /**
     * QuestionTechStack Entity의 @Id 필드명과 반드시 같아야 한다.
     */
    private Long techStackId;
}