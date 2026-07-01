package com.min.edu.team.repository;

import com.min.edu.team.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    // 내 팀 목록은 탈퇴한 팀원 관계와 삭제된 팀을 모두 제외한다.
    @Query("""
            select tm
            from TeamMember tm
            join fetch tm.team t
            where tm.user.id = :userId
              and tm.delflag = 'N'
              and t.delflag = 'N'
            order by t.createdAt desc
            """)
    List<TeamMember> findActiveTeamsByUserId(@Param("userId") Long userId);

    // 팀원 목록은 현재 활성 팀원만 조회하며, 사용자 정보를 함께 사용해 N+1 조회를 피한다.
    @Query("""
            select tm
            from TeamMember tm
            join fetch tm.user u
            where tm.team.id = :teamId
              and tm.delflag = 'N'
            order by tm.joinedAt asc
            """)
    List<TeamMember> findActiveMembersByTeamId(@Param("teamId") Long teamId);

    boolean existsByTeamIdAndUserIdAndDelflag(Long teamId, Long userId, String delflag);
}
