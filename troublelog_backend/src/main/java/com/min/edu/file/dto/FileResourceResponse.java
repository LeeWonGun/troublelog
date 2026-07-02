package com.min.edu.file.dto;

import org.springframework.core.io.Resource;

import lombok.AllArgsConstructor;
import lombok.Getter;

//이미지 바이너리 서빙에 필요한 최소 정보만 담는다. (Entity를 Controller로 그대로 노출하지 않기 위함)
@Getter
@AllArgsConstructor
public class FileResourceResponse {
	
	private Resource resource;
    private String contentType;

}
