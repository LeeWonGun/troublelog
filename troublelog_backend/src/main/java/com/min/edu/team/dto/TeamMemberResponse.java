package com.min.edu.team.dto;

import com.min.edu.team.domain.TeamMember;

import java.time.LocalDateTime;

public record TeamMemberResponse(
        Long userId,
        String nickname,
        String role,
        LocalDateTime joinedAt
) {

    public static TeamMemberResponse from(TeamMember teamMember) {
        return new TeamMemberResponse(
                teamMember.getUser().getId(),
                teamMember.getUser().getNickname(),
                teamMember.getRole().name(),
                teamMember.getJoinedAt()
        );
    }
}
