package com.min.edu.auth.service;

public interface EmailSender {

    void sendVerificationCode(String to, String subject, String code, int expiresMinutes);
}
