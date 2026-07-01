package com.min.edu.auth;

import com.min.edu.auth.domain.EmailVerificationCode;
import com.min.edu.auth.domain.EmailVerificationPurpose;
import com.min.edu.auth.repository.EmailVerificationCodeRepository;
import com.min.edu.auth.security.JwtCookieService;
import com.min.edu.auth.security.JwtTokenProvider;
import com.min.edu.user.domain.AuthProvider;
import com.min.edu.user.domain.User;
import com.min.edu.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailVerificationCodeRepository verificationCodeRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    void signupLoginAndGetMe() throws Exception {
        saveVerificationCode("signup@example.com", EmailVerificationPurpose.SIGNUP, "123456");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "signup@example.com",
                                  "password": "Abcd1234!",
                                  "nickname": "signupUser",
                                  "verificationCode": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("signup@example.com"))
                .andExpect(jsonPath("$.data.authProvider").value("LOCAL"));

        User savedUser = userRepository.findByEmailAndDelflag("signup@example.com", "N").orElseThrow();
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("Abcd1234!");
        assertThat(passwordEncoder.matches("Abcd1234!", savedUser.getPasswordHash())).isTrue();

        Cookie accessTokenCookie = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "signup@example.com",
                                  "password": "Abcd1234!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authType").value("JWT_COOKIE"))
                .andReturn()
                .getResponse()
                .getCookie(JwtCookieService.ACCESS_TOKEN_COOKIE_NAME);
        assertThat(accessTokenCookie).isNotNull();
        assertThat(accessTokenCookie.isHttpOnly()).isTrue();

        mockMvc.perform(get("/api/auth/me")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("signup@example.com"));
    }

    @Test
    void protectedApiRequiresJwtCookie() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void duplicateEmailAndNicknameAreRejected() throws Exception {
        userRepository.save(new User(
                "duplicate@example.com",
                passwordEncoder.encode("Abcd1234!"),
                "duplicateNick",
                AuthProvider.LOCAL,
                null
        ));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "duplicate@example.com",
                                  "password": "Abcd1234!",
                                  "nickname": "newNickname",
                                  "verificationCode": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new@example.com",
                                  "password": "Abcd1234!",
                                  "nickname": "duplicateNick",
                                  "verificationCode": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_NICKNAME"));
    }

    @Test
    void passwordResetWithEmailVerificationCode() throws Exception {
        userRepository.save(new User(
                "reset@example.com",
                passwordEncoder.encode("Abcd1234!"),
                "resetNick",
                AuthProvider.LOCAL,
                null
        ));
        saveVerificationCode("reset@example.com", EmailVerificationPurpose.PASSWORD_RESET, "654321");

        mockMvc.perform(post("/api/auth/password-reset/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "reset@example.com",
                                  "code": "654321"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified").value(true));

        mockMvc.perform(patch("/api/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "reset@example.com",
                                  "verificationCode": "654321",
                                  "newPassword": "Changed1234!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Cookie accessTokenCookie = loginAndGetAccessTokenCookie("reset@example.com", "Changed1234!");
        assertThat(accessTokenCookie).isNotNull();
    }

    @Test
    void googleUserCannotUseLocalLoginOrPasswordChange() throws Exception {
        User googleUser = userRepository.save(new User("google@example.com", null, "googleNick", AuthProvider.GOOGLE, "google-sub"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "google@example.com",
                                  "password": "anyPassword"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_AUTH_PROVIDER"));

        mockMvc.perform(patch("/api/users/me/password")
                        .cookie(accessTokenCookieFor(googleUser.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "Abcd1234!",
                                  "newPassword": "Newpass1234!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("GOOGLE_USER_PASSWORD_RESET_NOT_ALLOWED"));

        User localUser = userRepository.save(new User(
                "local@example.com",
                passwordEncoder.encode("Abcd1234!"),
                "localNick",
                AuthProvider.LOCAL,
                null
        ));

        Cookie localAccessTokenCookie = loginAndGetAccessTokenCookie("local@example.com", "Abcd1234!");
        mockMvc.perform(patch("/api/users/me/nickname")
                        .cookie(localAccessTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "changedNick"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("changedNick"));

        assertThat(localUser.getId()).isNotNull();
    }

    private Cookie loginAndGetAccessTokenCookie(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie(JwtCookieService.ACCESS_TOKEN_COOKIE_NAME);
    }

    private Cookie accessTokenCookieFor(Long userId) {
        return new Cookie(JwtCookieService.ACCESS_TOKEN_COOKIE_NAME, jwtTokenProvider.createToken(userId));
    }

    private void saveVerificationCode(String email, EmailVerificationPurpose purpose, String code) {
        verificationCodeRepository.save(new EmailVerificationCode(
                email,
                purpose,
                passwordEncoder.encode(code),
                LocalDateTime.now().plusMinutes(5)
        ));
    }
}
