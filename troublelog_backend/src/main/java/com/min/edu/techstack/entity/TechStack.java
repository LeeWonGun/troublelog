package com.min.edu.techstack.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 기술 스택 마스터 데이터를 관리하는 Entity이다.
 *
 * 질문 작성 화면의 기술 스택 체크박스,
 * 질문 검색 화면의 기술 스택 필터에 사용된다.
 */
@Entity
@Table(name = "tech_stacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // 프론트에서 기술 스택을 분류해서 보여줄 때 사용한다.
    @Column(nullable = false)
    private String category;

    // 비활성화된 기술 스택은 작성/검색 화면에 노출하지 않는다.
    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}