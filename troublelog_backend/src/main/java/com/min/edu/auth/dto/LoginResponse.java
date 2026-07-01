package com.min.edu.auth.dto;

import com.min.edu.user.dto.UserResponse;

public record LoginResponse(
        String authType,
        UserResponse user
) {

    public static LoginResponse jwtCookie(UserResponse user) {
        return new LoginResponse("JWT_COOKIE", user);
    }
}
