package com.min.edu.auth.security;

import com.min.edu.auth.dto.LoginResponse;
import com.min.edu.auth.service.AuthService;
import com.min.edu.common.exception.BusinessException;
import com.min.edu.user.dto.UserResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectProvider<AuthService> authServiceProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtCookieService jwtCookieService;
    private final String successRedirectUri;

    public GoogleOAuth2SuccessHandler(
            ObjectProvider<AuthService> authServiceProvider,
            JwtTokenProvider jwtTokenProvider,
            JwtCookieService jwtCookieService,
            @Value("${app.oauth2.success-redirect-uri:http://localhost:5173/mockup-api-test.html}") String successRedirectUri
    ) {
        this.authServiceProvider = authServiceProvider;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtCookieService = jwtCookieService;
        this.successRedirectUri = successRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String providerId = oauthUser.getAttribute("sub");

        if (email == null || providerId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Google account information is incomplete.");
            return;
        }

        LoginResponse loginResponse;
        try {
            loginResponse = authServiceProvider.getObject().loginOrSignupGoogle(email, providerId);
        } catch (BusinessException e) {
            response.sendRedirect(oauthErrorRedirectUri(e.getErrorCode().name()));
            return;
        }

        UserResponse user = loginResponse.user();
        String token = jwtTokenProvider.createToken(user.userId());
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookieService.accessTokenCookie(token).toString());

        String redirectUri = UriComponentsBuilder.fromUriString(successRedirectUri)
                .replaceQuery(null)
                .fragment(null)
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirectUri);
    }

    private String oauthErrorRedirectUri(String errorCode) {
        return UriComponentsBuilder.fromUriString(successRedirectUri)
                .replacePath("/login")
                .replaceQuery(null)
                .fragment(null)
                .queryParam("oauthError", errorCode)
                .build()
                .encode()
                .toUriString();
    }
}
