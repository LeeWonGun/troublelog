package com.min.edu.file.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.min.edu.auth.security.CurrentUser;
import com.min.edu.common.response.ApiResponse;
import com.min.edu.file.dto.FileResponse;
import com.min.edu.file.service.QuestionFileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class QuestionFileController {

	private final QuestionFileService questionFileService;
	
	// 질문 첨부 파일 업로드 (이미지 1개)
    @PostMapping("/api/questions/{questionId}/files")
    public ApiResponse<FileResponse> uploadFile(
            @PathVariable Long questionId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        Long userId = CurrentUser.id(authentication);
        FileResponse response = questionFileService.uploadFile(userId, questionId, file);

        return ApiResponse.success("파일이 업로드되었습니다.", response);
    }
	
}
