package com.min.edu.techstack.service;

import com.min.edu.techstack.dto.response.TechStackResponse;
import com.min.edu.techstack.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 기술 스택 관련 비즈니스 로직을 담당하는 Service이다.
 */
@Service
@RequiredArgsConstructor
public class TechStackService {

    private final TechStackRepository techStackRepository;

    /**
     * 질문 작성/검색 화면에서 사용할 기술 스택 목록을 조회한다.
     *
     * 비활성 기술 스택은 화면에 노출하지 않고,
     * 응답 DTO로 변환해서 Controller에 반환한다.
     */
    public List<TechStackResponse> getTechStacks() {
        return techStackRepository.findByActiveTrueOrderByCategoryAscNameAsc()
                .stream()
                .map(TechStackResponse::from)
                .toList();
    }
}