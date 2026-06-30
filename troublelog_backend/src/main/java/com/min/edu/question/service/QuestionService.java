package com.min.edu.question.service;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.question.dto.response.QuestionDetailResponse;
import com.min.edu.question.dto.response.QuestionListResponse;
import com.min.edu.question.entity.Question;
import com.min.edu.question.entity.QuestionTechStack;
import com.min.edu.question.repository.QuestionRepository;
import com.min.edu.question.repository.QuestionTechStackRepository;
import com.min.edu.question.util.QuestionContentFormatter;
import com.min.edu.question.util.QuestionContentParts;
import com.min.edu.techstack.dto.response.TechStackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 질문 조회 관련 비즈니스 로직을 담당하는 Service이다.
 *
 * 현재 단계에서는 비회원도 조회 가능한 PUBLIC 질문 목록과
 * PUBLIC 질문 상세 조회를 우선 구현한다.
 *
 * TEAM 질문 접근 권한 검사는 인증/팀 구조가 확정된 뒤
 * currentUserId와 teamId를 기준으로 추가한다.
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final String VISIBILITY_PUBLIC = "PUBLIC";
    private static final String DELFLAG_NORMAL = "N";

    private final QuestionRepository questionRepository;
    private final QuestionTechStackRepository questionTechStackRepository;

    /**
     * 비회원도 볼 수 있는 공개 질문 목록을 조회한다.
     *
     * PUBLIC 질문만 조회하고, delflag = 'N'인 정상 질문만 반환한다.
     * 질문에 연결된 기술 스택 목록은 questionId 기준으로 묶어서 함께 내려준다.
     */
    @Transactional(readOnly = true)
    public List<QuestionListResponse> getPublicQuestions() {
        List<Question> questions = questionRepository
                .findByVisibilityAndDelflagOrderByCreatedAtDesc(VISIBILITY_PUBLIC, DELFLAG_NORMAL);

        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<TechStackResponse>> techStackMap = getTechStackMap(questions);

        return questions.stream()
                .map(question -> QuestionListResponse.from(
                        question,
                        techStackMap.getOrDefault(question.getId(), Collections.emptyList())
                ))
                .toList();
    }

    /**
     * 질문 상세 정보를 조회한다.
     *
     * 현재는 PUBLIC 질문 상세 조회만 허용한다.
     * TEAM 질문은 팀원 권한 검사가 필요하므로 인증/팀 구조 확정 후 별도 처리한다.
     *
     * 상세 조회 성공 시 조회수를 1 증가시킨다.
     */
    @Transactional
    public QuestionDetailResponse getQuestionDetail(Long questionId) {
        Question question = questionRepository.findByIdAndDelflag(questionId, DELFLAG_NORMAL)
                .orElseThrow(() -> new BusinessException(
                        "질문을 찾을 수 없습니다.",
                        ErrorCode.QUESTION_NOT_FOUND
                ));

        validatePublicQuestion(question);

        question.increaseViewCount();

        QuestionContentParts contentParts = QuestionContentFormatter.parse(question.getContent());
        List<TechStackResponse> techStacks = getTechStacks(question.getId());

        return QuestionDetailResponse.from(question, contentParts, techStacks);
    }

    /**
     * 공개 질문 상세 조회에서는 PUBLIC 질문만 허용한다.
     *
     * TEAM 질문은 currentUserId와 teamId를 이용한 팀원 여부 확인이 필요하므로,
     * 용혁님 인증/팀 PR merge 후 팀 질문 상세 조회 로직에서 별도로 처리한다.
     */
    private void validatePublicQuestion(Question question) {
        if (!VISIBILITY_PUBLIC.equals(question.getVisibility())) {
            throw new BusinessException(
                    "팀 질문은 팀원만 조회할 수 있습니다.",
                    ErrorCode.FORBIDDEN
            );
        }
    }

    /**
     * 질문 목록에 연결된 기술 스택을 questionId 기준으로 묶어서 반환한다.
     *
     * 목록 조회에서 질문마다 기술 스택 Repository를 반복 호출하면
     * N+1 문제가 생길 수 있으므로, questionId 목록으로 한 번에 조회한 뒤 Map으로 묶는다.
     */
    private Map<Long, List<TechStackResponse>> getTechStackMap(List<Question> questions) {
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .toList();

        return questionTechStackRepository.findByQuestionIdIn(questionIds)
                .stream()
                .collect(Collectors.groupingBy(
                        QuestionTechStack::getQuestionId,
                        Collectors.mapping(
                                questionTechStack -> TechStackResponse.from(questionTechStack.getTechStack()),
                                Collectors.toList()
                        )
                ));
    }

    /**
     * 특정 질문에 연결된 기술 스택 목록을 조회한다.
     *
     * 질문 상세 화면에서 기술 스택 태그를 보여줄 때 사용한다.
     */
    private List<TechStackResponse> getTechStacks(Long questionId) {
        return questionTechStackRepository.findByQuestionId(questionId)
                .stream()
                .map(questionTechStack -> TechStackResponse.from(questionTechStack.getTechStack()))
                .toList();
    }
}