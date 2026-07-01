package com.min.edu.team.controller;

import com.min.edu.auth.security.CurrentUser;
import com.min.edu.common.response.ApiResponse;
import com.min.edu.team.dto.TeamCreateRequest;
import com.min.edu.team.dto.TeamJoinRequest;
import com.min.edu.team.dto.TeamMemberResponse;
import com.min.edu.team.dto.TeamResponse;
import com.min.edu.team.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 팀 생성, 참여, 조회, 탈퇴, 삭제 API를 제공하는 Controller이다.
 *
 * 모든 팀 API는 ACCESS_TOKEN 쿠키로 인증된 사용자만 호출할 수 있다.
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * 현재 로그인 사용자를 팀장으로 하는 새 팀을 생성한다.
     */
    @PostMapping
    public ApiResponse<TeamResponse> createTeam(
            Authentication authentication,
            @Valid @RequestBody TeamCreateRequest request
    ) {
        return ApiResponse.success("팀이 생성되었습니다.", teamService.createTeam(CurrentUser.id(authentication), request));
    }

    /**
     * 팀 코드로 팀에 참여한다.
     *
     * 이미 참여 중인 팀이면 중복 참여를 허용하지 않는다.
     */
    @PostMapping("/join")
    public ApiResponse<TeamResponse> joinTeam(
            Authentication authentication,
            @Valid @RequestBody TeamJoinRequest request
    ) {
        return ApiResponse.success("팀에 참여했습니다.", teamService.joinTeam(CurrentUser.id(authentication), request));
    }

    /**
     * 현재 로그인 사용자가 활성 팀원으로 참여 중인 팀 목록을 조회한다.
     */
    @GetMapping("/my")
    public ApiResponse<List<TeamResponse>> myTeams(Authentication authentication) {
        return ApiResponse.success("내 팀 목록 조회가 완료되었습니다.", teamService.getMyTeams(CurrentUser.id(authentication)));
    }

    /**
     * 팀 참여 전 입력한 팀 코드가 유효한지 확인한다.
     *
     * 미가입 사용자는 role 없이 팀 기본 정보만 응답받는다.
     */
    @GetMapping("/code/{teamCode}")
    public ApiResponse<TeamResponse> teamByCode(
            Authentication authentication,
            @PathVariable String teamCode
    ) {
        return ApiResponse.success("팀 코드 확인이 완료되었습니다.", teamService.getTeamByCode(CurrentUser.id(authentication), teamCode));
    }

    /**
     * 팀 상세 정보를 조회한다.
     *
     * 해당 팀의 활성 팀원만 조회할 수 있다.
     */
    @GetMapping("/{teamId}")
    public ApiResponse<TeamResponse> team(
            Authentication authentication,
            @PathVariable Long teamId
    ) {
        return ApiResponse.success("팀 상세 조회가 완료되었습니다.", teamService.getTeam(CurrentUser.id(authentication), teamId));
    }

    /**
     * 팀원 목록을 조회한다.
     *
     * 해당 팀의 활성 팀원만 조회할 수 있다.
     */
    @GetMapping("/{teamId}/members")
    public ApiResponse<List<TeamMemberResponse>> teamMembers(
            Authentication authentication,
            @PathVariable Long teamId
    ) {
        return ApiResponse.success("팀원 목록 조회가 완료되었습니다.", teamService.getTeamMembers(CurrentUser.id(authentication), teamId));
    }

    /**
     * 현재 로그인 사용자가 팀에서 탈퇴한다.
     *
     * 팀장은 탈퇴할 수 없고 팀 삭제로 처리해야 한다.
     */
    @PatchMapping("/{teamId}/leave")
    public ApiResponse<Void> leaveTeam(
            Authentication authentication,
            @PathVariable Long teamId
    ) {
        teamService.leaveTeam(CurrentUser.id(authentication), teamId);
        return ApiResponse.success("팀 탈퇴가 완료되었습니다.");
    }

    /**
     * 팀을 삭제한다.
     *
     * 팀장만 삭제할 수 있으며 실제 DELETE가 아니라 소프트 삭제로 처리한다.
     */
    @DeleteMapping("/{teamId}")
    public ApiResponse<Void> deleteTeam(
            Authentication authentication,
            @PathVariable Long teamId
    ) {
        teamService.deleteTeam(CurrentUser.id(authentication), teamId);
        return ApiResponse.success("팀을 삭제했습니다.");
    }
}
