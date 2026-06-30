package com.min.edu.answer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerCreateRequest {
	
	@NotBlank(message = "내용을 입력해주세요")
    private String content;

}
