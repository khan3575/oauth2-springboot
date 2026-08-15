package com.khan.oauth2springboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("!prod")
@Service
public class LoggingEmailService implements EmailService {

    private final String baseUrl;

    public LoggingEmailService(@Value("${app.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendVerificationEmail(String to, String rawToken) {
        log.info("Verification link for {}: {}/api/auth/verify-email?token={}", to, baseUrl, rawToken);
    }

    @Override
    public void sendPasswordResetEmail(String to, String rawToken) {
        log.info("Password reset link for {}: {}/api/auth/reset-password?token={}", to, baseUrl, rawToken);
    }
}
