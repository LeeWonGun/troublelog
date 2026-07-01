package com.min.edu.team.dto;

import com.min.edu.team.domain.Team;
import com.min.edu.team.domain.TeamMember;

import java.time.LocalDateTime;

public record TeamResponse(
        Long teamId,
        String name,
        String description,
        String teamCode,
        String role,
        Long ownerId,
        LocalDateTime createdAt
) {

    public static TeamResponse from(TeamMember teamMember) {
        Team team = teamMember.getTeam();
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getTeamCode(),
                teamMember.getRole().name(),
                team.getOwner().getId(),
                team.getCreatedAt()
        );
    }

    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getTeamCode(),
                null,
                team.getOwner().getId(),
                team.getCreatedAt()
        );
    }

    public static TeamResponse from(Team team, TeamMember teamMember) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getTeamCode(),
                teamMember.getRole().name(),
                team.getOwner().getId(),
                team.getCreatedAt()
        );
    }
}
