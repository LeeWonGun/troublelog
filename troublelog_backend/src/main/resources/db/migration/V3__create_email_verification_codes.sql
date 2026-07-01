CREATE TABLE email_verification_codes
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(100) NOT NULL,
    purpose     VARCHAR(30)  NOT NULL,
    code_hash   VARCHAR(255) NOT NULL,
    expires_at  DATETIME     NOT NULL,
    verified_at DATETIME NULL,
    consumed_at DATETIME NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_email_verification_codes_purpose CHECK (purpose IN ('SIGNUP', 'PASSWORD_RESET'))
);

CREATE INDEX idx_email_verification_codes_email_purpose_created_at
    ON email_verification_codes (email, purpose, created_at);
