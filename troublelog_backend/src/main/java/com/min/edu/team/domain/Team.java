package com.min.edu.team.domain;

import com.min.edu.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "team_code", nullable = false, unique = true, length = 50)
    private String teamCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // 팀 삭제는 실제 DELETE 대신 delflag로 처리해 기존 참여 이력과 팀 코드를 보존한다.
    @Column(nullable = false, columnDefinition = "CHAR(1)")
    private String delflag = "N";

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Team() {
    }

    public Team(String name, String description, String teamCode, User owner) {
        this.name = name;
        this.description = description;
        this.teamCode = teamCode;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public User getOwner() {
        return owner;
    }

    public String getDelflag() {
        return delflag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isDeleted() {
        return "Y".equals(delflag);
    }

    public boolean isOwner(Long userId) {
        return owner.getId().equals(userId);
    }

    public void delete() {
        this.delflag = "Y";
        this.updatedAt = LocalDateTime.now();
    }
}
