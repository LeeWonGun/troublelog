package com.min.edu.auth.service;

import com.min.edu.common.exception.BusinessException;
import com.min.edu.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "resend")
public class ResendEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailSender.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String from;
    private final boolean enabled;

    public ResendEmailSender(
            @Value("${app.mail.resend.api-key:}") String apiKey,
            @Value("${app.mail.from:TroubleLog <onboarding@resend.dev>}") String from,
            @Value("${app.mail.enabled:true}") boolean enabled
    ) {
        this.restClient = RestClient.create("https://api.resend.com");
        this.apiKey = apiKey;
        this.from = from;
        this.enabled = enabled;
    }

    @Override
    public void sendVerificationCode(String to, String subject, String code, int expiresMinutes) {
        if (!enabled) {
            return;
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("Resend API 키가 설정되어 있지 않습니다.", ErrorCode.MAIL_SEND_FAILED);
        }

        try {
            restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(Map.of(
                            "from", from,
                            "to", to,
                            "subject", subject,
                            "html", verificationHtml(code, expiresMinutes)
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.warn("Resend email send failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException("인증 메일 발송에 실패했습니다.", ErrorCode.MAIL_SEND_FAILED);
        } catch (Exception e) {
            log.warn("Resend email send failed.", e);
            throw new BusinessException("인증 메일 발송에 실패했습니다.", ErrorCode.MAIL_SEND_FAILED);
        }
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
