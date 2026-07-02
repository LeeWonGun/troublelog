package com.min.edu.question.service;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.question.entity.QuestionTechStack;
import com.min.edu.question.repository.QuestionTechStackRepository;
import com.min.edu.techstack.dto.response.TechStackResponse;
import com.min.edu.techstack.repository.TechStackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 질문과 기술 스택 연결의 저장, 교체, 응답 조회를 담당하는 서비스입니다.
 */
@Service
@Transactional(readOnly = true)
public class QuestionTechStackService {

    private final QuestionTechStackRepository questionTechStackRepository;
    private final TechStackRepository techStackRepository;

    public QuestionTechStackService(
            QuestionTechStackRepository questionTechStackRepository,
            TechStackRepository techStackRepository
    ) {
        this.questionTechStackRepository = questionTechStackRepository;
        this.techStackRepository = techStackRepository;
    }

    @Transactional
    public void saveTechStacks(Long questionId, List<Long> techStackIds) {
        List<Long> normalizedTechStackIds = normalizeTechStackIds(techStackIds);

        if (normalizedTechStackIds.isEmpty()) {
            return;
        }

        validateActiveTechStacks(normalizedTechStackIds);

        List<QuestionTechStack> questionTechStacks = normalizedTechStackIds.stream()
                .map(techStackId -> new QuestionTechStack(questionId, techStackId))
                .toList();

        questionTechStackRepository.saveAll(questionTechStacks);
    }

    @Transactional
    public void replaceTechStacks(Long questionId, List<Long> techStackIds) {
        questionTechStackRepository.deleteByQuestionId(questionId);
        saveTechStacks(questionId, techStackIds);
    }

    public List<TechStackResponse> getTechStacks(Long questionId) {
        return questionTechStackRepository.findByQuestionIdWithTechStack(questionId)
                .stream()
                .map(questionTechStack -> TechStackResponse.from(questionTechStack.getTechStack()))
                .toList();
    }

    public Map<Long, List<TechStackResponse>> getTechStackMap(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return questionTechStackRepository.findByQuestionIdInWithTechStack(questionIds)
                .stream()
                .collect(Collectors.groupingBy(
                        QuestionTechStack::getQuestionId,
                        Collectors.mapping(
                                questionTechStack -> TechStackResponse.from(questionTechStack.getTechStack()),
                                Collectors.toList()
                        )
                ));
    }

    private void validateActiveTechStacks(List<Long> techStackIds) {
        long activeCount = techStackRepository.countByIdInAndActiveTrue(techStackIds);
        if (activeCount != techStackIds.size()) {
            throw new BusinessException("기술 스택 ID가 올바르지 않습니다.", ErrorCode.INVALID_REQUEST);
        }
    }

    private List<Long> normalizeTechStackIds(List<Long> techStackIds) {
        if (techStackIds == null) {
            return Collections.emptyList();
        }

        return techStackIds.stream()
                .filter(techStackId -> techStackId != null && techStackId > 0)
                .distinct()
                .toList();
    }
}
