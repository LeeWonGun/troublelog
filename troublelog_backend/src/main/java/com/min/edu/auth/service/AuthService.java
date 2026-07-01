package com.min.edu.auth.service;

import com.min.edu.auth.domain.EmailVerificationPurpose;
import com.min.edu.auth.dto.DuplicateCheckResponse;
import com.min.edu.auth.dto.LoginRequest;
import com.min.edu.auth.dto.LoginResponse;
import com.min.edu.auth.dto.PasswordResetRequest;
import com.min.edu.auth.dto.SignupRequest;
import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.user.domain.AuthProvider;
import com.min.edu.user.domain.User;
import com.min.edu.user.dto.UserResponse;
import com.min.edu.user.repository.UserRepository;
import com.min.edu.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * 회원가입, 로그인, 중복 검사, 비밀번호 재설정 비즈니스 로직을 담당하는 Service이다.
 *
 * 로컬 계정은 서비스 비밀번호를 사용하고, Google 계정은 OAuth2 providerId로 식별한다.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final String RANDOM_NICKNAME_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            UserService userService,
            PasswordEncoder passwordEncoder,
            EmailVerificationService emailVerificationService
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * 이메일 인증을 완료한 로컬 사용자를 가입 처리한다.
     *
     * 가입 시 이메일과 닉네임은 삭제되지 않은 사용자 기준으로 중복될 수 없다.
     */
    @Transactional
    public UserResponse signup(SignupRequest request) {
        validateEmailAvailable(request.email());
        validateNicknameAvailable(request.nickname());
        emailVerificationService.consumeVerifiedCode(
                request.email(),
                EmailVerificationPurpose.SIGNUP,
                request.verificationCode()
        );

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                AuthProvider.LOCAL,
                null
        );

        return UserResponse.from(userRepository.save(user));
    }

    /**
     * 로컬 계정의 이메일/비밀번호 로그인을 처리한다.
     *
     * Google 계정은 서비스 비밀번호가 없으므로 일반 로그인 대상에서 제외한다.
     */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDelflag(request.email(), "N")
                .orElseThrow(() -> new BusinessException("이메일 또는 비밀번호가 일치하지 않습니다.", ErrorCode.INVALID_PASSWORD));

        if (!user.isLocalUser()) {
            throw new BusinessException("Google 계정은 Google 로그인으로 이용해 주세요.", ErrorCode.INVALID_AUTH_PROVIDER);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("이메일 또는 비밀번호가 일치하지 않습니다.", ErrorCode.INVALID_PASSWORD);
        }

        return LoginResponse.jwtCookie(UserResponse.from(user));
    }

    /**
     * 현재 로그인 사용자의 정보를 조회한다.
     */
    public UserResponse getMe(Long userId) {
        return userService.getMyInfo(userId);
    }

    /**
     * 이메일이 가입에 사용 가능한지 확인한다.
     */
    public DuplicateCheckResponse checkEmail(String email) {
        return new DuplicateCheckResponse(!userRepository.existsByEmailAndDelflag(email, "N"));
    }

    /**
     * 닉네임이 가입에 사용 가능한지 확인한다.
     */
    public DuplicateCheckResponse checkNickname(String nickname) {
        return new DuplicateCheckResponse(!userRepository.existsByNicknameAndDelflag(nickname, "N"));
    }

    /**
     * 이메일 인증번호 검증을 통과한 사용자의 비밀번호를 재설정한다.
     *
     * Google 계정은 외부 인증을 사용하므로 서비스 비밀번호 재설정을 허용하지 않는다.
     */
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByEmailAndDelflag(request.email(), "N")
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

        if (!user.isLocalUser()) {
            throw new BusinessException("Google 계정은 비밀번호 재설정을 사용할 수 없습니다.", ErrorCode.GOOGLE_USER_PASSWORD_RESET_NOT_ALLOWED);
        }

        emailVerificationService.consumeVerifiedCode(
                request.email(),
                EmailVerificationPurpose.PASSWORD_RESET,
                request.verificationCode()
        );
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    /**
     * Google OAuth2 사용자 정보를 기준으로 로그인하거나 신규 가입시킨다.
     *
     * 신규 사용자는 이메일을 닉네임으로 쓰지 않고 랜덤 닉네임을 부여한다.
     */
    @Transactional
    public LoginResponse loginOrSignupGoogle(String email, String providerId) {
        User user = userRepository.findByAuthProviderAndProviderIdAndDelflag(AuthProvider.GOOGLE, providerId, "N")
                .orElseGet(() -> userRepository.save(new User(
                        email,
                        null,
                        generateRandomNickname(),
                        AuthProvider.GOOGLE,
                        providerId
                )));

        return LoginResponse.jwtCookie(UserResponse.from(user));
    }

    /**
     * 삭제되지 않은 사용자 중 같은 이메일이 있으면 가입을 막는다.
     */
    private void validateEmailAvailable(String email) {
        if (userRepository.existsByEmailAndDelflag(email, "N")) {
            throw new BusinessException("이미 사용 중인 이메일입니다.", ErrorCode.DUPLICATE_EMAIL);
        }
    }

    /**
     * 삭제되지 않은 사용자 중 같은 닉네임이 있으면 가입을 막는다.
     */
    private void validateNicknameAvailable(String nickname) {
        if (userRepository.existsByNicknameAndDelflag(nickname, "N")) {
            throw new BusinessException("이미 사용 중인 닉네임입니다.", ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    /**
     * Google 신규 사용자에게 부여할 중복 없는 랜덤 닉네임을 생성한다.
     */
    private String generateRandomNickname() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String nickname = "user" + randomAlphaNumeric(8);
            if (!userRepository.existsByNicknameAndDelflag(nickname, "N")) {
                return nickname;
            }
        }

        throw new BusinessException("랜덤 닉네임 생성에 실패했습니다.", ErrorCode.DUPLICATE_NICKNAME);
    }

    private String randomAlphaNumeric(int length) {
        StringBuilder value = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            value.append(RANDOM_NICKNAME_CHARS.charAt(secureRandom.nextInt(RANDOM_NICKNAME_CHARS.length())));
        }
        return value.toString();
    }
}
