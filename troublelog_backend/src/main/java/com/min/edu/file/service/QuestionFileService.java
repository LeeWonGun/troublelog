package com.min.edu.file.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.file.FileConstants;
import com.min.edu.file.dto.FileResponse;
import com.min.edu.file.entity.QuestionFileEntity;
import com.min.edu.file.repository.QuestionFileMapper;
import com.min.edu.file.repository.QuestionFileRepository;
import com.min.edu.file.storage.FileStorage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionFileService {
	
	private final QuestionFileMapper questionFileMapper;
	private final QuestionFileRepository questionFileRepository;
	private final FileStorage fileStorage;
	
	
	// 질문 첨부 파일을 업로드한다. (이미지 1개 제한)
	@Transactional
	public FileResponse uploadFile(Long userId, Long questionId, MultipartFile file) {
		
		validateWriter(questionId, userId);
		validateFile(file);
		
		int activeExists = questionFileMapper.existsActiveFile(questionId);
		
		if(activeExists > 0) {
			throw new BusinessException("이미 첨부된 파일이 있습니다.", ErrorCode.DUPLICATE_RESOURCE);
		}
		
		String extenstion = extractExtension(file.getOriginalFilename());
		String uploadFilename = UUID.randomUUID() + "." + extenstion;
		
		String filePath = fileStorage.store(file, uploadFilename);
		String fileUrl = fileStorage.resolveUrl(uploadFilename);
		
		QuestionFileEntity entity = new QuestionFileEntity(
				questionId, 
				file.getOriginalFilename(), 
				uploadFilename, 
				filePath, 
				fileUrl, 
				file.getContentType(), 
				file.getSize()
		);
		
		
		questionFileMapper.insertFile(entity);
		
		return toResponse(entity);
		
	}
	
	
	// 질문 작성자인지 확인한다.
	private void validateWriter(Long questionId, Long userId) {
		
		int isWriter = questionFileMapper.existsQuestionWriter(questionId, userId);
		
		if(isWriter == 0) {
			throw new BusinessException("존재하지 않거나 작성 권한이 없는 질문입니다.", ErrorCode.QUESTION_NOT_FOUND);
		}
	
	}
	
	
	
	// 파일 확장자와 용량을 검증한다.
	private void validateFile(MultipartFile file) {
		
		if(file == null || file.isEmpty()) {
			throw new BusinessException("첨부할 파일이 없습니다.", ErrorCode.INVALID_REQUEST);
		}
		
		if(file.getSize() > FileConstants.MAX_FILE_SIZE) {
			throw new BusinessException("파일 크기는 5MB를 초과할 수 없습니다.", ErrorCode.FILE_SIZE_EXCEEDED);
		}
		
		String extension = extractExtension(file.getOriginalFilename());
		
		if(!FileConstants.ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
			throw new BusinessException("jpg, jpeg, png 파일만 첨부할 수 있습니다.", ErrorCode.INVALID_FILE_TYPE);
		}
			
	}
	
	
	// 파일명에서 확장자를 추출한다.
    private String extractExtension(String originalFilename) {
    	
    	if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException("파일 확장자를 확인할 수 없습니다.", ErrorCode.INVALID_FILE_TYPE);
        }
    	
    	return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }
    
    
    private FileResponse toResponse(QuestionFileEntity file) {
        return new FileResponse(
                file.getId(),
                file.getQuestionId(),
                file.getOriginalFilename(),
                file.getFileUrl(),
                file.getContentType(),
                file.getFileSize(),
                file.getCreatedAt()
        );
    }
    
    
    // 첨부 파일을 조회한다. 질문 조회 권한과 동일하게 처리하며, 비회원도 PUBLIC 질문 파일은 조회 가능하다.
    public FileResponse getFile(Long fileId, Long userId) {
    	
    	QuestionFileEntity file = questionFileRepository.findById(fileId)
    			.filter(QuestionFileEntity::isActive)
    			.orElseThrow(() -> new BusinessException("존재하지 않는 파일입니다.", ErrorCode.FILE_NOT_FOUND));
    	
    	int accessible = questionFileMapper.existsAccessibleQuestion(file.getQuestionId(), userId);
    	
    	if (accessible == 0) {
    		throw new BusinessException("존재하지 않거나 접근할 수 없는 파일입니다.", ErrorCode.FILE_NOT_FOUND);
    	}
    	
    	return toResponse(file);
    }
	

}
