package com.min.edu.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.min.edu.file.entity.QuestionFileEntity;

public interface QuestionFileRepository extends JpaRepository<QuestionFileEntity, Long> {

	
}
