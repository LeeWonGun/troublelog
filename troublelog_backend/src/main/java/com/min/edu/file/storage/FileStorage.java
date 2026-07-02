package com.min.edu.file.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 실제 파일 저장소에 대한 추상화이다.
 * 현재는 로컬 파일시스템 구현체만 사용하지만,
 * 추후 S3 등으로 교체할 때 이 인터페이스만 구현하면 되도록 분리했다.
 */
public interface FileStorage {
	
	// 파일을 저장하고, 저장된 경로(file_path)를 반환한다.
	String store(MultipartFile file, String uploadFilename);
	
	// 저장된 파일명을 바탕으로 외부에서 접근 가능한 URL(file_url)을 생성한다.
	String resolveUrl(String uploadFilename);

}
