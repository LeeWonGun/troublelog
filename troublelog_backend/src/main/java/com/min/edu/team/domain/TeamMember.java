package com.min.edu.team.domain;

import com.min.edu.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_members")
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TeamRole role;

    @Column(name = "joined_at", nullable = false, insertable = false)
    private LocalDateTime joinedAt;

    // 재참여 시 기존 row를 복구하므로 탈퇴 시점만 초기화 대상이 된다.
    @Column(name = "leaved_at")
    private LocalDateTime leavedAt;

    // 팀원 탈퇴는 UNIQUE(user_id, team_id)를 유지하기 위해 소프트 삭제로 처리한다.
    @Column(nullable = false, columnDefinition = "CHAR(1)")
    private String delflag = "N";

    protected TeamMember() {
    }

    public TeamMember(User user, Team team, TeamRole role) {
        this.user = user;
        this.team = team;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Team getTeam() {
        return team;
    }

    public TeamRole getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public String getDelflag() {
        return delflag;
    }

    public boolean isActive() {
        return "N".equals(delflag);
    }

    public boolean isLeader() {
        return role == TeamRole.LEADER;
    }

    public void leave() {
        this.delflag = "Y";
        this.leavedAt = LocalDateTime.now();
    }

    public void restoreAsMember() {
        this.role = TeamRole.MEMBER;
        this.delflag = "N";
        this.leavedAt = null;
    }
}
