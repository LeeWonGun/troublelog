package com.min.edu.team.repository;

import com.min.edu.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByTeamCode(String teamCode);

    boolean existsByTeamCode(String teamCode);
}
