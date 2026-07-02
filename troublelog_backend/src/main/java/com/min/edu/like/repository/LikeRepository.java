package com.min.edu.like.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.min.edu.like.entity.LikeEntity;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
	
	// 특정 사용자가 특정 대상에 이미 좋아요를 눌렀는지 확인한다.
	Optional<LikeEntity> findByUserIdAndTargetIdAndTargetType(
			Long userId, Long targetId, LikeEntity.TargetType targetType);

}
