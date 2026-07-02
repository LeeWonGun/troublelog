package com.min.edu.like.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeResponse {
	
	private boolean liked; // 등록 후: true, 취소 후: false
    private int likeCount; // 갱신된 좋아요 수

}
