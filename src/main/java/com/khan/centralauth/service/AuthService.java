package com.khan.centralauth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.khan.centralauth.dto.RegisterRequest;
import com.khan.centralauth.entity.AppUser;
import com.khan.centralauth.entity.AuditLog;
import com.khan.centralauth.entity.Credential;
import com.khan.centralauth.entity.EmailVerificationToken;
import com.khan.centralauth.entity.enums.CredentialType;
import com.khan.centralauth.entity.enums.UserStatus;
import com.khan.centralauth.repository.AppUserRepository;
import com.khan.centralauth.repository.AuditLogRepository;
import com.khan.centralauth.repository.CredentialRepository;
import com.khan.centralauth.repository.EmailVerificationTokenRepository;

import jakarta.transaction.Transactional;


@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final CredentialRepository credentialRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository,
                       CredentialRepository credentialRepository,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       AuditLogRepository auditLogRepository,
                       PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.credentialRepository = credentialRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public void register(RegisterRequest request)
    {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if(appUserRepository.findByEmail(normalizedEmail).isPresent())
        {
            // why return even though the user already exists?
            // This is to prevent the user from knowing whether the email is already registered or not.
            return; 
        }
        
        OffsetDateTime now = OffsetDateTime.now();
        
        // create a new user with the provided email and default values for other fields
        AppUser user = AppUser.builder()
            .email(normalizedEmail)
            .emailVerified(false)
            .status(UserStatus.PENDING_VERIFICATION)
            .build();
        
        user = appUserRepository.save(user);

        // create a new credential for the user

        Credential credential = Credential.builder()
            .userId(user.getId())
            .type(CredentialType.PASSWORD)
            .secretHash(passwordEncoder.encode(request.getPassword()))
            .createdAt(now)
            .build();
        credential = credentialRepository.save(credential);

        String rawToken = UUID.randomUUID().toString();
        
        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder()
            .userId(user.getId())
            .tokenHash(hashToken(rawToken))
            .expiresAt(now.plusHours(24))
            .createdAt(now)
            .build();

        emailVerificationTokenRepository.save(emailVerificationToken);

        // now audit loging

        AuditLog auditLog = AuditLog.builder()
            .userId(user.getId())
            .eventType("register")
            .createdAt(now)
            .build();
        auditLogRepository.save(auditLog);
    }


    private String hashToken(String rawToken) {
        
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        }
        catch(NoSuchAlgorithmException e)
        {
            throw new IllegalArgumentException("SHA-256 not available", e);
        }
    }

}
