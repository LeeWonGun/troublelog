package com.min.edu.auth.service;

import com.min.edu.auth.domain.EmailVerificationCode;
import com.min.edu.auth.domain.EmailVerificationPurpose;
import com.min.edu.auth.repository.EmailVerificationCodeRepository;
import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import com.min.edu.user.domain.User;
import com.min.edu.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 회원가입과 비밀번호 재설정에 사용하는 이메일 인증번호 발급/검증 로직을 담당하는 Service이다.
 *
 * 인증번호 원문은 저장하지 않고 BCrypt 해시로 저장한다.
 */
@Service
@Transactional(readOnly = true)
public class EmailVerificationService {

    private final EmailVerificationCodeRepository verificationCodeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final SecureRandom secureRandom = new SecureRandom();
    private final int expiresMinutes;

    public EmailVerificationService(
            EmailVerificationCodeRepository verificationCodeRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailSender emailSender,
            @Value("${app.mail.verification-code-expiration-minutes:5}") int expiresMinutes
    ) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.expiresMinutes = expiresMinutes;
    }

    /**
     * 회원가입 이메일 인증번호를 발급하고 메일로 발송한다.
     *
     * 이미 가입된 이메일에는 회원가입 인증번호를 발급하지 않는다.
     */
    @Transactional
    public void sendSignupCode(String email) {
        if (userRepository.existsByEmailAndDelflag(email, "N")) {
            throw new BusinessException("이미 사용 중인 이메일입니다.", ErrorCode.DUPLICATE_EMAIL);
        }
        issueCode(email, EmailVerificationPurpose.SIGNUP, "[TroubleLog] 회원가입 이메일 인증번호");
    }

    /**
     * 비밀번호 재설정 인증번호를 발급하고 메일로 발송한다.
     *
     * Google 계정은 서비스 비밀번호가 없으므로 재설정 인증번호를 발급하지 않는다.
     */
    @Transactional
    public void sendPasswordResetCode(String email) {
        User user = getActiveUser(email);
        validateLocalUser(user);
        issueCode(email, EmailVerificationPurpose.PASSWORD_RESET, "[TroubleLog] 비밀번호 재설정 인증번호");
    }

    /**
     * 인증번호가 유효한지 확인하고 검증 완료 상태로 표시한다.
     */
    @Transactional
    public void verifyCode(String email, EmailVerificationPurpose purpose, String code) {
        EmailVerificationCode verificationCode = getLatestUsableCode(email, purpose);
        validateCode(verificationCode, code);
        verificationCode.verify();
    }

    /**
     * 회원가입 또는 비밀번호 재설정 완료 시 인증번호를 소비 처리한다.
     *
     * 이미 검증된 코드가 아니더라도 값이 일치하면 검증 후 소비한다.
     */
    @Transactional
    public void consumeVerifiedCode(String email, EmailVerificationPurpose purpose, String code) {
        EmailVerificationCode verificationCode = getLatestUsableCode(email, purpose);
        validateCode(verificationCode, code);
        if (!verificationCode.isVerified()) {
            verificationCode.verify();
        }
        verificationCode.consume();
    }

    /**
     * 6자리 인증번호를 생성하고 해시만 저장한 뒤 메일로 발송한다.
     */
    private void issueCode(String email, EmailVerificationPurpose purpose, String subject) {
        String code = generateCode();
        verificationCodeRepository.save(new EmailVerificationCode(
                email,
                purpose,
                passwordEncoder.encode(code),
                LocalDateTime.now().plusMinutes(expiresMinutes)
        ));
        emailSender.sendVerificationCode(email, subject, code, expiresMinutes);
    }

    /**
     * 아직 소비되지 않은 가장 최신 인증번호를 조회한다.
     */
    private EmailVerificationCode getLatestUsableCode(String email, EmailVerificationPurpose purpose) {
        return verificationCodeRepository.findTopByEmailAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new BusinessException("유효한 인증번호가 없습니다.", ErrorCode.INVALID_VERIFICATION_CODE));
    }

    /**
     * 인증번호 만료 여부와 입력값 일치 여부를 검증한다.
     */
    private void validateCode(EmailVerificationCode verificationCode, String code) {
        if (verificationCode.isExpired(LocalDateTime.now())) {
            throw new BusinessException("인증번호가 만료되었습니다.", ErrorCode.EXPIRED_VERIFICATION_CODE);
        }
        if (!passwordEncoder.matches(code, verificationCode.getCodeHash())) {
            throw new BusinessException("인증번호가 일치하지 않습니다.", ErrorCode.INVALID_VERIFICATION_CODE);
        }
    }

    /**
     * 비밀번호 재설정 대상이 되는 활성 사용자를 조회한다.
     */
    private User getActiveUser(String email) {
        return userRepository.findByEmailAndDelflag(email, "N")
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));
    }

    /**
     * Google 계정은 서비스 비밀번호가 없으므로 로컬 계정만 허용한다.
     */
    private void validateLocalUser(User user) {
        if (!user.isLocalUser()) {
            throw new BusinessException("Google 계정은 비밀번호 재설정을 사용할 수 없습니다.", ErrorCode.GOOGLE_USER_PASSWORD_RESET_NOT_ALLOWED);
        }
    }

    /**
     * 메일로 전송할 6자리 숫자 인증번호를 생성한다.
     */
    private String generateCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }
}
