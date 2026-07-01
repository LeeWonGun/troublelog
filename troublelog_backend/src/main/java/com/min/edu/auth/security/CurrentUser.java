package com.min.edu.auth.security;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new BusinessException("로그인이 필요합니다.", ErrorCode.UNAUTHORIZED);
        }

        return userId;
    }
}
