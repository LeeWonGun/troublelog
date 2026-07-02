package com.min.edu.question.service;

import com.min.edu.common.response.PageResponse;
import com.min.edu.like.entity.LikeEntity;
import com.min.edu.like.repository.LikeRepository;
import com.min.edu.question.dto.response.QuestionDetailResponse;
import com.min.edu.question.dto.response.QuestionListResponse;
import com.min.edu.question.dto.response.QuestionSearchRow;
import com.min.edu.question.entity.Question;
import com.min.edu.question.util.QuestionContentParts;
import com.min.edu.team.domain.Team;
import com.min.edu.team.repository.TeamRepository;
import com.min.edu.techstack.dto.response.TechStackResponse;
import com.min.edu.user.domain.User;
import com.min.edu.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 질문 Entity와 조회 Row를 API 응답 DTO로 조립하는 컴포넌트입니다.
 */
@Component
public class QuestionResponseAssembler {

    private final QuestionTechStackService questionTechStackService;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final LikeRepository likeRepository;

    public QuestionResponseAssembler(
            QuestionTechStackService questionTechStackService,
            UserRepository userRepository,
            TeamRepository teamRepository,
            LikeRepository likeRepository
    ) {
        this.questionTechStackService = questionTechStackService;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.likeRepository = likeRepository;
    }

    public QuestionDetailResponse toDetailResponse(
            Question question,
            QuestionContentParts contentParts,
            Long currentUserId
    ) {
        List<TechStackResponse> techStacks = questionTechStackService.getTechStacks(question.getId());
        String writerNickname = getWriterNickname(question.getWriterId());
        String teamName = getTeamName(question.getTeamId());
        boolean likedByMe = isLikedByMe(question.getId(), currentUserId);

        return QuestionDetailResponse.from(
                question,
                contentParts,
                techStacks,
                writerNickname,
                teamName,
                likedByMe
        );
    }

    public PageResponse<QuestionListResponse> toPageResponse(
            Page<Question> questionPage,
            int page,
            int size
    ) {
        return new PageResponse<>(
                toQuestionListResponses(questionPage.getContent()),
                page,
                size,
                questionPage.getTotalElements(),
                questionPage.getTotalPages(),
                questionPage.hasNext()
        );
    }

    public List<QuestionListResponse> toQuestionListResponsesFromSearchRows(List<QuestionSearchRow> rows) {
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = rows.stream()
                .map(QuestionSearchRow::questionId)
                .toList();

        Map<Long, List<TechStackResponse>> techStackMap = questionTechStackService.getTechStackMap(questionIds);

        return rows.stream()
                .map(row -> QuestionListResponse.fromSearchRow(
                        row,
                        techStackMap.getOrDefault(row.questionId(), Collections.emptyList())
                ))
                .toList();
    }

    private List<QuestionListResponse> toQuestionListResponses(List<Question> questions) {
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .toList();

        Map<Long, List<TechStackResponse>> techStackMap = questionTechStackService.getTechStackMap(questionIds);
        Map<Long, String> writerNicknameMap = getWriterNicknameMap(questions);

        return questions.stream()
                .map(question -> QuestionListResponse.from(
                        question,
                        writerNicknameMap.get(question.getWriterId()),
                        techStackMap.getOrDefault(question.getId(), Collections.emptyList())
                ))
                .toList();
    }

    private Map<Long, String> getWriterNicknameMap(List<Question> questions) {
        List<Long> writerIds = questions.stream()
                .map(Question::getWriterId)
                .distinct()
                .toList();

        if (writerIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userRepository.findAllById(writerIds)
                .stream()
                .filter(user -> !user.isDeleted())
                .collect(Collectors.toMap(User::getId, User::getNickname));
    }

    private String getWriterNickname(Long writerId) {
        if (writerId == null) {
            return null;
        }

        return userRepository.findById(writerId)
                .filter(user -> !user.isDeleted())
                .map(User::getNickname)
                .orElse(null);
    }

    private String getTeamName(Long teamId) {
        if (teamId == null) {
            return null;
        }

        return teamRepository.findById(teamId)
                .filter(team -> !team.isDeleted())
                .map(Team::getName)
                .orElse(null);
    }

    private boolean isLikedByMe(Long questionId, Long currentUserId) {
        if (currentUserId == null) {
            return false;
        }

        return likeRepository.existsByUserIdAndTargetIdAndTargetType(
                currentUserId,
                questionId,
                LikeEntity.TargetType.QUE
        );
    }
}
