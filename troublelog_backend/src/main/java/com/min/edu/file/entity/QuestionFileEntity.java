package com.min.edu.file.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "question_files")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionFileEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    // 서버에 저장된 파일명으로 UUID 기반 파일명 사용
    @Column(name = "upload_filename", nullable = false)
    private String uploadFilename;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    // 조회용 URL
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    // byte 기준
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
	
    // N: 정상, Y: 삭제
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private DelFlag delflag;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 실제 파일 삭제 예정 일시 deleted_at + 7일
    @Column(name = "purge_due_at")
    private LocalDateTime purgeDueAt;

    // 실제 서버 파일 삭제 완료 일시(스케줄러가 파일 삭제 성공 후 기록)
    @Column(name = "purged_at")
    private LocalDateTime purgedAt;
	
	
    public QuestionFileEntity(
            Long questionId,
            String originalFilename,
            String uploadFilename,
            String filePath,
            String fileUrl,
            String contentType,
            Long fileSize
    ) {
        this.questionId = questionId;
        this.originalFilename = originalFilename;
        this.uploadFilename = uploadFilename;
        this.filePath = filePath;
        this.fileUrl = fileUrl;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.delflag = DelFlag.N;
        this.createdAt = LocalDateTime.now();
    }
    
    
    // 소프트 삭제 처리: delflag를 Y로 바꾸고 삭제 시점/실제 삭제 예정 시점을 기록한다.
    public void softDelete() {
        this.delflag = DelFlag.Y;
        this.deletedAt = LocalDateTime.now();
        this.purgeDueAt = this.deletedAt.plusDays(7);
    }

    
    public boolean isActive() {
        return this.delflag == DelFlag.N;
    }
    
    
    public enum DelFlag {
        N, Y
    }

}
