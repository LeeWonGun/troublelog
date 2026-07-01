package com.min.edu.auth.service;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "smtp", matchIfMissing = true)
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final boolean enabled;
    private final String username;

    public SmtpEmailSender(
            JavaMailSender mailSender,
            @Value("${app.mail.from:}") String from,
            @Value("${app.mail.enabled:true}") boolean enabled,
            @Value("${spring.mail.username:}") String username
    ) {
        this.mailSender = mailSender;
        this.from = from;
        this.enabled = enabled;
        this.username = username;
    }

    @Override
    public void sendVerificationCode(String to, String subject, String code, int expiresMinutes) {
        if (!enabled) {
            return;
        }
        if (username == null || username.isBlank()) {
            throw new BusinessException("SMTP 계정이 설정되어 있지 않습니다.", ErrorCode.MAIL_SEND_FAILED);
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(resolveFrom());
            helper.setText(verificationHtml(code, expiresMinutes), true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            log.warn("SMTP email send failed.", e);
            throw new BusinessException("인증 메일 발송에 실패했습니다.", ErrorCode.MAIL_SEND_FAILED);
        }
    }

    private String resolveFrom() {
        if (from != null && !from.isBlank()) {
            return from;
        }
        return username;
    }

    private String verificationHtml(String code, int expiresMinutes) {
        return """
                <div style="font-family:Arial,sans-serif;line-height:1.6;color:#111827">
                  <h2>TroubleLog 이메일 인증</h2>
                  <p>아래 인증번호를 입력해 주세요.</p>
                  <div style="font-size:28px;font-weight:700;letter-spacing:6px;margin:20px 0">%s</div>
                  <p>인증번호는 %d분 후 만료됩니다.</p>
                </div>
                """.formatted(code, expiresMinutes);
    }
}
