package com.min.edu.answer.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
public class AnswerResponse {

	private Long id;
    private Long questionId;
    private Long writerId;
    // 화면에 보여줄 닉네임
    private String writerNickname;
    private Long parentAnswerId;
    private int depth;
    private String content;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
	
    // 답변이면 자신에게 달린 댓글, 댓글이면 자신에게 달린 대댓글을 담는다.
    @Setter
    private List<AnswerResponse> children;
    
    
    // MyBatis가 SELECT 컬럼(10개)을 이 생성자로 자동 매핑할 수 있도록, children을 제외한 생성자를 별도로 둔다.
    public AnswerResponse(Long id, Long questionId, Long writerId, String writerNickname,
                           Long parentAnswerId, int depth, String content, int likeCount,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.questionId = questionId;
        this.writerId = writerId;
        this.writerNickname = writerNickname;
        this.parentAnswerId = parentAnswerId;
        this.depth = depth;
        this.content = content;
        this.likeCount = likeCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
}
