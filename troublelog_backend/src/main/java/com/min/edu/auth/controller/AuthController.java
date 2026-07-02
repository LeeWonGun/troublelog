package com.min.edu.auth.controller;

import com.min.edu.auth.domain.EmailVerificationPurpose;
import com.min.edu.auth.dto.response.DuplicateCheckResponse;
import com.min.edu.auth.dto.response.EmailVerificationResponse;
import com.min.edu.auth.dto.request.EmailVerificationSendRequest;
import com.min.edu.auth.dto.request.EmailVerificationVerifyRequest;
import com.min.edu.auth.dto.request.LoginRequest;
import com.min.edu.auth.dto.response.LoginResponse;
import com.min.edu.auth.dto.request.PasswordResetRequest;
import com.min.edu.auth.dto.request.SignupRequest;
import com.min.edu.auth.security.CurrentUser;
import com.min.edu.auth.security.JwtCookieService;
import com.min.edu.auth.security.JwtTokenProvider;
import com.min.edu.auth.service.AuthService;
import com.min.edu.auth.service.EmailVerificationService;
import com.min.edu.common.response.ApiResponse;
import com.min.edu.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원가입, 로그인, 이메일 인증, 비밀번호 재설정 API를 제공하는 Controller이다.
 *
 * 로그인 성공 시 JWT는 응답 본문이 아니라 ACCESS_TOKEN HttpOnly Cookie로 전달한다.
 */
@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtCookieService jwtCookieService;

    public AuthController(
            AuthService authService,
            EmailVerificationService emailVerificationService,
            JwtTokenProvider jwtTokenProvider,
            JwtCookieService jwtCookieService
    ) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtCookieService = jwtCookieService;
    }

    /**
     * 이메일 인증이 완료된 사용자의 회원가입을 처리한다.
     */
    @PostMapping("/signup")
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success("회원가입이 완료되었습니다.", authService.signup(request));
    }

    /**
     * 일반 이메일/비밀번호 로그인을 처리하고 ACCESS_TOKEN 쿠키를 발급한다.
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse
    ) {
        LoginResponse response = authService.login(request);
        String token = jwtTokenProvider.createToken(response.user().userId());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, jwtCookieService.accessTokenCookie(token).toString());
        return ApiResponse.success("로그인되었습니다.", response);
    }

    /**
     * ACCESS_TOKEN 쿠키를 만료시켜 로그아웃을 처리한다.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookieService.expiredAccessTokenCookie().toString());
        return ApiResponse.success("로그아웃되었습니다.");
    }

    /**
     * 현재 ACCESS_TOKEN 쿠키로 인증된 사용자 정보를 조회한다.
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(Authentication authentication) {
        return ApiResponse.success("현재 로그인 사용자 조회가 완료되었습니다.", authService.getMe(CurrentUser.id(authentication)));
    }

    /**
     * 회원가입 전에 이메일 사용 가능 여부를 확인한다.
     */
    @GetMapping("/check-email")
    public ApiResponse<DuplicateCheckResponse> checkEmail(
            @RequestParam @NotBlank @Email String email
    ) {
        return ApiResponse.success("이메일 중복 확인이 완료되었습니다.", authService.checkEmail(email));
    }

    /**
     * 회원가입 또는 닉네임 변경 전에 닉네임 사용 가능 여부를 확인한다.
     */
    @GetMapping("/check-nickname")
    public ApiResponse<DuplicateCheckResponse> checkNickname(
            @RequestParam @NotBlank String nickname
    ) {
        return ApiResponse.success("닉네임 중복 확인이 완료되었습니다.", authService.checkNickname(nickname));
    }

    /**
     * 회원가입에 사용할 이메일 인증번호를 발송한다.
     */
    @PostMapping("/signup/send-code")
    public ApiResponse<Void> sendSignupVerificationCode(@Valid @RequestBody EmailVerificationSendRequest request) {
        emailVerificationService.sendSignupCode(request.email());
        return ApiResponse.success("회원가입 인증 메일을 발송했습니다.");
    }

    /**
     * 회원가입 이메일 인증번호가 유효한지 확인한다.
     */
    @PostMapping("/signup/verify-code")
    public ApiResponse<EmailVerificationResponse> verifySignupCode(@Valid @RequestBody EmailVerificationVerifyRequest request) {
        emailVerificationService.verifyCode(request.email(), EmailVerificationPurpose.SIGNUP, request.code());
        return ApiResponse.success("이메일 인증이 완료되었습니다.", new EmailVerificationResponse(true));
    }

    /**
     * 비밀번호 재설정에 사용할 이메일 인증번호를 발송한다.
     */
    @PostMapping("/password-reset/send-code")
    public ApiResponse<Void> sendPasswordResetCode(@Valid @RequestBody EmailVerificationSendRequest request) {
        emailVerificationService.sendPasswordResetCode(request.email());
        return ApiResponse.success("비밀번호 재설정 인증 메일을 발송했습니다.");
    }

    /**
     * 비밀번호 재설정 인증번호가 유효한지 확인한다.
     */
    @PostMapping("/password-reset/verify-code")
    public ApiResponse<EmailVerificationResponse> verifyPasswordResetCode(@Valid @RequestBody EmailVerificationVerifyRequest request) {
        emailVerificationService.verifyCode(request.email(), EmailVerificationPurpose.PASSWORD_RESET, request.code());
        return ApiResponse.success("비밀번호 재설정 인증이 완료되었습니다.", new EmailVerificationResponse(true));
    }

    /**
     * 인증번호 검증을 통과한 사용자의 비밀번호를 재설정한다.
     */
    @PatchMapping("/password-reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("비밀번호가 재설정되었습니다.");
    }
}
