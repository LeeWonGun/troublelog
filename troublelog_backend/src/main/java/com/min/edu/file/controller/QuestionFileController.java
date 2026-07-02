package com.min.edu.file.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.min.edu.auth.security.CurrentUser;
import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.common.response.ApiResponse;
import com.min.edu.file.dto.FileResourceResponse;
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
    
    
    // 첨부 파일 메타데이터 조회 (PUBLIC 질문은 비회원도 조회 가능)
 	@GetMapping("/api/files/{fileId}")
 	public ApiResponse<FileResponse> getFile(
 			@PathVariable Long fileId,
 			Authentication authentication
 	) {
 		
 		Long userId = resolveUserIdOrNull(authentication);
 		FileResponse response = questionFileService.getFile(fileId, userId);
 		
 		return ApiResponse.success("조회가 완료되었습니다.", response);
 	}
 	
 	
 	// 첨부 파일 삭제 (작성자만 가능)
 	@DeleteMapping("/api/files/{fileId}")
 	public ApiResponse<Void> deleteFile(
 			@PathVariable Long fileId,
 			Authentication authentication
 	) {
 		
 		Long userId = CurrentUser.id(authentication);
 		questionFileService.deleteFile(fileId, userId);
 		
 		return ApiResponse.success("파일이 삭제되었습니다.");
 	}
 	
 	
 	/*
 	 * 첨부 이미지 실제 바이너리를 서빙한다. <img src="..."> 에서 브라우저가 직접 호출하는 경로이다.
 	 * 메타데이터 조회(GET /api/files/{fileId})와 동일한 접근 제어를 적용한다.
 	 */
 	@GetMapping("/api/files/static/{uploadFilename}")
 	public ResponseEntity<Resource> serveFile(
 			@PathVariable String uploadFilename,
 			Authentication authentication
 	) {
 		
 		Long userId = resolveUserIdOrNull(authentication);
 		FileResourceResponse result = questionFileService.getFileResource(uploadFilename, userId);

 		return ResponseEntity.ok()
 				.contentType(MediaType.parseMediaType(result.getContentType()))
 				.body(result.getResource());
 	}
 	
 	
 	// 비회원은 UNAUTHORIZED만 잡아 null 처리한다. (PUBLIC 리소스는 비회원도 접근 가능해야 하므로)
 	private Long resolveUserIdOrNull(Authentication authentication) {
 		try {
 			return CurrentUser.id(authentication);
 		} catch (BusinessException e) {
 			if (e.getErrorCode() != ErrorCode.UNAUTHORIZED) {
 				throw e;
 			}
 			return null;
 		}
 	}
 	
 	
 	// 질문 첨부 파일 교체 (작성자만 가능, 기존 파일은 소프트 삭제 후 새 파일 등록)
 	@PutMapping("/api/questions/{questionId}/files")
 	public ApiResponse<FileResponse> replaceFile(
 			@PathVariable Long questionId,
 			@RequestParam("file") MultipartFile file,
 			Authentication authentication
 	) {
 		
 		Long userId = CurrentUser.id(authentication);
 		
 		FileResponse response = questionFileService.replaceFile(userId, questionId, file);
 		
 		return ApiResponse.success("파일이 교체되었습니다.", response);
 	}
 	
	
}
