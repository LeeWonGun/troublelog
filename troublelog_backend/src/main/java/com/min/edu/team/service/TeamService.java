package com.min.edu.team.service;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.team.domain.Team;
import com.min.edu.team.domain.TeamMember;
import com.min.edu.team.domain.TeamRole;
import com.min.edu.team.dto.TeamCreateRequest;
import com.min.edu.team.dto.TeamJoinRequest;
import com.min.edu.team.dto.TeamMemberResponse;
import com.min.edu.team.dto.TeamResponse;
import com.min.edu.team.repository.TeamMemberRepository;
import com.min.edu.team.repository.TeamRepository;
import com.min.edu.user.domain.User;
import com.min.edu.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

/**
 * 팀 생성, 참여, 조회, 탈퇴, 삭제 비즈니스 로직을 담당하는 Service이다.
 *
 * 팀 삭제와 팀원 탈퇴는 실제 DELETE가 아니라 delflag 기반 소프트 삭제로 처리한다.
 */
@Service
@Transactional(readOnly = true)
public class TeamService {

    private static final String TEAM_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserService userService;
    private final SecureRandom secureRandom = new SecureRandom();

    public TeamService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            UserService userService
    ) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userService = userService;
    }

    /**
     * 현재 사용자를 팀장으로 하는 팀을 생성한다.
     *
     * 팀 생성 시 팀 코드와 팀장 멤버 row를 함께 생성한다.
     */
    @Transactional
    public TeamResponse createTeam(Long userId, TeamCreateRequest request) {
        User owner = userService.getActiveUser(userId);
        Team team = teamRepository.save(new Team(request.name(), request.description(), generateTeamCode(), owner));
        TeamMember leader = teamMemberRepository.save(new TeamMember(owner, team, TeamRole.LEADER));

        return TeamResponse.from(team, leader);
    }

    /**
     * 팀 코드로 팀에 참여한다.
     *
     * 삭제된 팀 코드는 사용할 수 없고, 탈퇴 이력이 있으면 기존 팀원 row를 복구한다.
     */
    @Transactional
    public TeamResponse joinTeam(Long userId, TeamJoinRequest request) {
        User user = userService.getActiveUser(userId);
        Team team = teamRepository.findByTeamCode(request.teamCode())
                .orElseThrow(() -> new BusinessException("유효하지 않은 팀 코드입니다.", ErrorCode.INVALID_TEAM_CODE));

        if (team.isDeleted()) {
            throw new BusinessException("유효하지 않은 팀 코드입니다.", ErrorCode.INVALID_TEAM_CODE);
        }

        TeamMember teamMember = teamMemberRepository.findByTeamIdAndUserId(team.getId(), userId)
                .map(this::restoreOrReject)
                .orElseGet(() -> teamMemberRepository.save(new TeamMember(user, team, TeamRole.MEMBER)));

        return TeamResponse.from(team, teamMember);
    }

    /**
     * 현재 사용자가 활성 팀원으로 참여 중인 팀 목록을 조회한다.
     */
    public List<TeamResponse> getMyTeams(Long userId) {
        userService.getActiveUser(userId);
        return teamMemberRepository.findActiveTeamsByUserId(userId)
                .stream()
                .map(TeamResponse::from)
                .toList();
    }

    /**
     * 팀 상세 정보를 조회한다.
     *
     * 해당 팀의 활성 팀원만 조회할 수 있다.
     */
    public TeamResponse getTeam(Long userId, Long teamId) {
        TeamMember member = requireActiveMember(teamId, userId);
        return TeamResponse.from(member.getTeam(), member);
    }

    /**
     * 팀 참여 전 팀 코드가 유효한지 확인한다.
     *
     * 이미 활성 팀원이면 role을 포함하고, 미가입 사용자는 role 없이 응답한다.
     */
    public TeamResponse getTeamByCode(Long userId, String teamCode) {
        userService.getActiveUser(userId);
        Team team = teamRepository.findByTeamCode(teamCode)
                .orElseThrow(() -> new BusinessException("유효하지 않은 팀 코드입니다.", ErrorCode.INVALID_TEAM_CODE));

        if (team.isDeleted()) {
            throw new BusinessException("유효하지 않은 팀 코드입니다.", ErrorCode.INVALID_TEAM_CODE);
        }

        return teamMemberRepository.findByTeamIdAndUserId(team.getId(), userId)
                .filter(TeamMember::isActive)
                .map(teamMember -> TeamResponse.from(team, teamMember))
                .orElseGet(() -> TeamResponse.from(team));
    }

    /**
     * 팀원 목록을 조회한다.
     *
     * 해당 팀의 활성 팀원만 조회할 수 있다.
     */
    public List<TeamMemberResponse> getTeamMembers(Long userId, Long teamId) {
        requireActiveMember(teamId, userId);
        return teamMemberRepository.findActiveMembersByTeamId(teamId)
                .stream()
                .map(TeamMemberResponse::from)
                .toList();
    }

    /**
     * 현재 사용자를 팀에서 탈퇴 처리한다.
     *
     * MVP에서는 팀장이 팀을 나가려면 팀 삭제로 처리한다.
     */
    @Transactional
    public void leaveTeam(Long userId, Long teamId) {
        TeamMember member = requireActiveMember(teamId, userId);

        if (member.isLeader()) {
            throw new BusinessException("팀장은 팀을 탈퇴할 수 없습니다.", ErrorCode.CANNOT_LEAVE_LEADER);
        }

        member.leave();
    }

    /**
     * 팀을 삭제 처리한다.
     *
     * 팀장만 삭제할 수 있으며 팀과 활성 팀원 관계를 모두 소프트 삭제한다.
     */
    @Transactional
    public void deleteTeam(Long userId, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("팀을 찾을 수 없습니다.", ErrorCode.TEAM_NOT_FOUND));

        if (team.isDeleted()) {
            throw new BusinessException("팀을 찾을 수 없습니다.", ErrorCode.TEAM_NOT_FOUND);
        }

        if (!team.isOwner(userId)) {
            throw new BusinessException("팀장만 팀을 삭제할 수 있습니다.", ErrorCode.FORBIDDEN);
        }

        team.delete();
        teamMemberRepository.findActiveMembersByTeamId(teamId).forEach(TeamMember::leave);
    }

    /**
     * 팀 내부 정보 접근 전에 현재 사용자가 해당 팀의 활성 팀원인지 확인한다.
     */
    private TeamMember requireActiveMember(Long teamId, Long userId) {
        return teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .filter(TeamMember::isActive)
                .filter(teamMember -> !teamMember.getTeam().isDeleted())
                .orElseThrow(() -> new BusinessException("팀원만 접근할 수 있습니다.", ErrorCode.FORBIDDEN));
    }

    /**
     * 이미 참여 중이면 중복 참여를 막고, 탈퇴 이력이 있으면 기존 row를 복구한다.
     */
    private TeamMember restoreOrReject(TeamMember existingMember) {
        if (existingMember.isActive()) {
            throw new BusinessException("이미 가입된 팀입니다.", ErrorCode.ALREADY_JOINED_TEAM);
        }

        existingMember.restoreAsMember();
        return existingMember;
    }

    /**
     * 팀 참여에 사용할 중복 없는 6자리 팀 코드를 생성한다.
     */
    private String generateTeamCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String code = randomAlphaNumeric(6);
            if (!teamRepository.existsByTeamCode(code)) {
                return code;
            }
        }

        throw new BusinessException("팀 코드 생성에 실패했습니다.", ErrorCode.DUPLICATE_RESOURCE);
    }

    private String randomAlphaNumeric(int length) {
        StringBuilder value = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            value.append(TEAM_CODE_CHARS.charAt(secureRandom.nextInt(TEAM_CODE_CHARS.length())));
        }
        return value.toString();
    }
}
