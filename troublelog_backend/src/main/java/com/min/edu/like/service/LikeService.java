package com.min.edu.like.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.like.dto.LikeResponse;
import com.min.edu.like.entity.LikeEntity;
import com.min.edu.like.repository.LikeMapper;
import com.min.edu.like.repository.LikeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {
	
	private final LikeRepository likeRepository;
	private final LikeMapper likeMapper;
	
	// 질문 좋아요를 등록한다. 이미 눌려있으면 중복 삽입 없이 그대로 둔다(idempotent).
	@Transactional
	public LikeResponse likeQuestion(Long userId, Long questionId) {
		
		validateQuestionAccessible(questionId, userId);
		
		boolean alreadyLiked = likeRepository
				.findByUserIdAndTargetIdAndTargetType(userId, questionId, LikeEntity.TargetType.QUE)
				.isPresent();
		
		if(!alreadyLiked) {
			likeMapper.insertIfNotExists(userId, questionId, LikeEntity.TargetType.QUE.name());
			likeMapper.updateQuestionCount(questionId);
		}
		
		int likeCount = likeMapper.selectQuestionLikeCount(questionId);
		
		return new LikeResponse(true, likeCount);
		
	}
	
	
	// 답변 좋아요를 등록한다. depth=0인 답변만 가능하다.
    @Transactional
    public LikeResponse likeAnswer(Long userId, Long answerId) {
    	
    	validateAnswerAccessible(answerId, userId);
    	
    	
    	boolean alreadyLiked = likeRepository
    			.findByUserIdAndTargetIdAndTargetType(userId, answerId, LikeEntity.TargetType.ANS)
    			.isPresent();
    	
    	if(!alreadyLiked) {
    		likeMapper.insertIfNotExists(userId, answerId, LikeEntity.TargetType.ANS.name());
    		likeMapper.updateAnswerCount(answerId);
    	}
    	
    	int likeCount = likeMapper.selectAnswerLikeCount(answerId);
    	
    	return new LikeResponse(true, likeCount);
    	
    }
    
    
    // 질문이 존재하고, 삭제되지 않았고, 요청자가 조회 권한이 있는지 확인한다.
    private void validateQuestionAccessible(Long questionId, Long userId) {
    	
    	int accessible = likeMapper.existsAccessibleQuestion(questionId, userId);
    	
    	if(accessible == 0) {
    		throw new BusinessException("존재하지 않거나 접근할 수 없는 질문입니다.", ErrorCode.QUESTION_NOT_FOUND);
    	}

    }
    
    
    // 답변이 존재하고, depth=0이고, 요청자가 조회 권한이 있는지 확인한다.
    private void validateAnswerAccessible(Long answerId, Long userId) {
    	
    	int accessible = likeMapper.existsAccessibleAnswer(answerId, userId);
    	
    	if(accessible == 0) {
    		throw new BusinessException("존재하지 않거나 접근할 수 없는 답변입니다.", ErrorCode.ANSWER_NOT_FOUND);
    	}
    }

    
	// 질문 좋아요를 취소한다.
    @Transactional
    public LikeResponse unlikeQuestion(Long userId, Long questionId) {
    	LikeEntity like = likeRepository
    			.findByUserIdAndTargetIdAndTargetType(userId, questionId, LikeEntity.TargetType.QUE)
    			.orElseThrow(() -> new BusinessException("좋아요를 누른 기록이 없습니다.", ErrorCode.LIKE_NOT_FOUND));
    	
    	likeRepository.delete(like);
    	likeRepository.flush();   // ← 추가: JPA가 지금 즉시 DELETE를 DB로 내보내도록 강제
    	likeMapper.updateQuestionCount(questionId);
    	
    	int likeCount = likeMapper.selectQuestionLikeCount(questionId);
    	
    	return new LikeResponse(false, likeCount);
    	
    }
    
    
    // 답변 좋아요를 취소한다.
    @Transactional
    public LikeResponse unlikeAnswer(Long userId, Long answerId) {
    	
    	LikeEntity like = likeRepository
    			.findByUserIdAndTargetIdAndTargetType(userId, answerId, LikeEntity.TargetType.ANS)
    			.orElseThrow(() -> new BusinessException("좋아요를 누른 기록이 없습니다.", ErrorCode.LIKE_NOT_FOUND));
    	
    	likeRepository.delete(like);
    	likeRepository.flush();   // ← 추가: JPA가 지금 즉시 DELETE를 DB로 내보내도록 강제
    	likeMapper.updateAnswerCount(answerId);
    	
    	int likeCount = likeMapper.selectAnswerLikeCount(answerId);
    	
    	return new LikeResponse(false, likeCount);
    }
    
}
