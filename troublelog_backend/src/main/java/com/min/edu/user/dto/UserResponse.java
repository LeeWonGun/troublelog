package com.min.edu.user.dto;

import com.min.edu.user.domain.User;

public record UserResponse(
        Long userId,
        String email,
        String nickname,
        String authProvider
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getAuthProvider().name()
        );
    }
}
