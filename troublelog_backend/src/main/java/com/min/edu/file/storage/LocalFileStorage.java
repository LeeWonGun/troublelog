package com.min.edu.file.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;

@Component
public class LocalFileStorage implements FileStorage {
	
	@Value("${file.upload-dir}")
	private String uploadDir;
	
	@Value("${file.base-url}")
	private String baseUrl;

	
	@Override
	public String store(MultipartFile file, String uploadFilename) {
		
		try {
			Path dir = Paths.get(uploadDir);
			
			if(!Files.exists(dir)) {
				Files.createDirectories(dir);
			}
			
			Path target = dir.resolve(uploadFilename);
			file.transferTo(target);
			
			return target.toString();
		} catch(IOException e) {
			throw new BusinessException("파일 저장에 실패했습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
		}
		
	}

	
	@Override
	public String resolveUrl(String uploadFilename) {
		
		return baseUrl + "/" + uploadFilename;
		
	}

}
