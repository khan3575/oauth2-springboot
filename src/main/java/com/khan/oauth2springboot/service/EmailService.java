package com.khan.oauth2springboot.service;

public interface EmailService {
    void sendVerificationEmail(String to, String rawToken);
    void sendPasswordResetEmail(String to, String rawToken);
}
