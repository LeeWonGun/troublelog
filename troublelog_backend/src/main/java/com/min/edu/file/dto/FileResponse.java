package com.min.edu.file.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileResponse {

	private Long id;
	private Long questionId;
	private String originalFilename;
	private String fileUrl;
	private String contentType;
	private Long fileSize;
	private LocalDateTime createdAt;
	
}
