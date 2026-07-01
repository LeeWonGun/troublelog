package com.min.edu.user.repository;

import com.min.edu.user.domain.AuthProvider;
import com.min.edu.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndDelflag(String email, String delflag);

    Optional<User> findByAuthProviderAndProviderIdAndDelflag(AuthProvider authProvider, String providerId, String delflag);

    boolean existsByEmailAndDelflag(String email, String delflag);

    boolean existsByNicknameAndDelflag(String nickname, String delflag);
}
