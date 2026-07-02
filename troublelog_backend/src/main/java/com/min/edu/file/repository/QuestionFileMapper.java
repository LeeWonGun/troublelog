package com.min.edu.file.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.min.edu.file.entity.QuestionFileEntity;

@Mapper
public interface QuestionFileMapper {

	// 해당 질문에 활성(delflag='N') 파일이 이미 존재하는지 확인한다. (질문당 활성 파일 1개 제한)
	int existsActiveFile(@Param("questionId") Long questionId);
	
	
	// 파일을 업로드(INSERT)한다. useGeneratedKeys로 INSERT 후 id를 엔티티에 채워 넣는다.
	void insertFile(QuestionFileEntity file);
	
	
	// 질문 작성자인지 확인한다. (파일 업로드/교체/삭제 권한 체크용)
	int existsQuestionWriter(@Param("questionId") Long questionId, @Param("userId") Long userId);
	
	
	// 질문 조회 권한이 있는지 확인한다. PUBLIC이면 통과, TEAM이면 팀원인지 확인한다. (파일 조회용)
	int existsAccessibleQuestion(@Param("questionId") Long questionId, @Param("userId") Long userId);
	
	
	// 물리 삭제 대상 파일 목록을 조회한다. (소프트 삭제 + 삭제 예정일 경과 + 아직 물리 삭제 안 됨)
	List<QuestionFileEntity> selectPurgeTargets();
	
	
	// 물리 삭제 완료 시각(purged_at)을 기록한다.
	void updatePurgedAt(@Param("id") Long id);
	
}
