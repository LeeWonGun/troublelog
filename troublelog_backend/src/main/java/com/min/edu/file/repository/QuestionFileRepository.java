package com.min.edu.file.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.min.edu.file.entity.QuestionFileEntity;

public interface QuestionFileRepository extends JpaRepository<QuestionFileEntity, Long> {

	// URL에 담긴 uploadFilename(UUID.확장자)으로 실제 DB row를 찾기 위한 메서드이다.
	Optional<QuestionFileEntity> findByUploadFilename(String uploadFilename);
}
