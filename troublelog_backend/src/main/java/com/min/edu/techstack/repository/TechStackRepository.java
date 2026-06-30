package com.min.edu.techstack.repository;

import com.min.edu.techstack.entity.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * tech_stacks 테이블 조회를 담당하는 Repository이다.
 */
public interface TechStackRepository extends JpaRepository<TechStack, Long> {

    /**
     * 질문 작성/검색 화면에 보여줄 활성 기술 스택 목록을 조회한다.
     *
     * active = true인 데이터만 조회하고,
     * 프론트에서 분류별로 보여주기 쉽도록 category, name 순서로 정렬한다.
     */
    List<TechStack> findByActiveTrueOrderByCategoryAscNameAsc();
}