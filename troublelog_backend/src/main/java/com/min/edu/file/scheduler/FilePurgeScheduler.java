package com.min.edu.file.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.min.edu.file.entity.QuestionFileEntity;
import com.min.edu.file.repository.QuestionFileMapper;
import com.min.edu.file.storage.FileStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 소프트 삭제된 첨부 파일을 일정 기간(7일) 후 실제로 삭제하는 스케줄러이다.
 * DB row는 지우지 않고 purged_at만 기록해 삭제 이력을 남긴다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FilePurgeScheduler {
	
	private final QuestionFileMapper questionFileMapper;
	private final FileStorage fileStorage;
	
	// 매일 새벽 3시에 실행한다.
	@Scheduled(cron = "0 0 3 * * *")
	
	public void purgeExpiredFiles() {
		
		// 삭제 예정일이 지난 파일 목록 조회
		List<QuestionFileEntity> targets = questionFileMapper.selectPurgeTargets();
		
		if(targets.isEmpty()) {
			return;
		}
		
		log.info("파일 물리 삭제 시작: 대상 {}건", targets.size());
		
		int success = 0;
		
		for(QuestionFileEntity file : targets) {
			
			try {
				
				// 실제 디스크에서 파일 삭제
				fileStorage.delete(file.getFilePath());
				
				// 삭제 완료 시각 기록 (이게 안 찍히면 다음 실행 때 자동 재시도된다)
				questionFileMapper.updatePurgedAt(file.getId());
				
				success++; 
				
			} catch(Exception e) {
				
				// 파일 하나 실패해도 나머지는 계속 처리한다.
				log.error("파일 물리 삭제 실패: id={}, path={}", file.getId(), file.getFilePath(), e);
			}
		}
		
		log.info("파일 물리 삭제 완료: {}/{}건 성공", success, targets.size());
		
	}
	
}
