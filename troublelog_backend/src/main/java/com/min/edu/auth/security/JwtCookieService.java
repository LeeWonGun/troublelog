package com.min.edu.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class JwtCookieService {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "ACCESS_TOKEN";

    private final JwtTokenProvider jwtTokenProvider;
    private final boolean secure;
    private final String sameSite;

    public JwtCookieService(
            JwtTokenProvider jwtTokenProvider,
            @Value("${app.jwt.cookie.secure:false}") boolean secure,
            @Value("${app.jwt.cookie.same-site:Lax}") String sameSite
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public ResponseCookie accessTokenCookie(String token) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(jwtTokenProvider.expirationSeconds())
                .build();
    }

    public ResponseCookie expiredAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0)
                .build();
    }
}
