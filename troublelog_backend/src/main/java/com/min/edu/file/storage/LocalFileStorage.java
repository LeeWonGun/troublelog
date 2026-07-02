package com.min.edu.file.storage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
	
	
	/*
	 * filePath(DB에 저장된 실제 경로 문자열)를 실제 파일 시스템 경로로 변환하고,
	 * 그 파일이 실제로 존재하는지, 읽을 수 있는지 확인한 뒤 Resource로 감싸서 반환한다.
	 */
	@Override
    public Resource loadAsResource(String filePath) {
        try {
            // 문자열 경로를 자바가 다룰 수 있는 Path 객체로 변환
            Path path = Paths.get(filePath);

            // 그 경로를 가리키는 Resource 객체 생성 (아직 파일을 다 읽지는 않음, 손잡이만 만든 상태)
            Resource resource = new UrlResource(path.toUri());

            // 파일이 실제로 없거나(삭제됐거나), 권한 문제로 못 읽으면 에러 처리
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException("파일을 찾을 수 없습니다.", ErrorCode.FILE_NOT_FOUND);
            }

            return resource;
            
        } catch (MalformedURLException e) {
            // filePath 문자열 자체가 이상한 형식이라 URL로 못 바꿀 때
            throw new BusinessException("파일 경로가 올바르지 않습니다.", ErrorCode.FILE_NOT_FOUND);
        }
    }
	
	// 파일이 이미 없어도 에러 없이 넘어간다. (스케줄러가 파일 하나 때문에 멈추지 않도록)
	@Override
	public void delete(String filePath) {
	    try {
	        Files.deleteIfExists(Paths.get(filePath));
	    } catch (IOException e) {
	        throw new BusinessException("파일 삭제에 실패했습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
	    }
	}

}
