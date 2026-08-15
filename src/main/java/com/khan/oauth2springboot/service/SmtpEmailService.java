package com.khan.oauth2springboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("prod")
@Service
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final String baseUrl;
    private final String fromAddress;

    public SmtpEmailService(JavaMailSender mailSender,
                             @Value("${app.base-url}") String baseUrl,
                             @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendVerificationEmail(String to, String rawToken) {
        send(to, "Verify your email",
            "Verify your email by visiting: " + baseUrl + "/api/auth/verify-email?token=" + rawToken);
    }

    @Override
    public void sendPasswordResetEmail(String to, String rawToken) {
        send(to, "Reset your password",
            "Reset your password by visiting: " + baseUrl + "/api/auth/reset-password?token=" + rawToken);
    }

    private void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        log.info("Sent \"{}\" email to {}", subject, to);
    }
}
