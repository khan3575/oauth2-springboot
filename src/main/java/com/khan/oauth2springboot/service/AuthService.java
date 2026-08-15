package com.khan.oauth2springboot.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.khan.oauth2springboot.dto.LoginRequest;
import com.khan.oauth2springboot.dto.RegisterRequest;
import com.khan.oauth2springboot.entity.AppUser;
import com.khan.oauth2springboot.entity.Credential;
import com.khan.oauth2springboot.entity.EmailVerificationToken;
import com.khan.oauth2springboot.entity.PasswordResetToken;
import com.khan.oauth2springboot.entity.Session;
import com.khan.oauth2springboot.entity.enums.CredentialType;
import com.khan.oauth2springboot.entity.enums.UserStatus;
import com.khan.oauth2springboot.repository.AppUserRepository;
import com.khan.oauth2springboot.repository.CredentialRepository;
import com.khan.oauth2springboot.repository.EmailVerificationTokenRepository;
import com.khan.oauth2springboot.repository.PasswordResetTokenRepository;
import com.khan.oauth2springboot.repository.SessionRepository;

import jakarta.transaction.Transactional;


@Slf4j
@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final SessionRepository sessionRepository;
    private final CredentialRepository credentialRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    


    public AuthService(AppUserRepository appUserRepository,
                       SessionRepository sessionRepository,
                       CredentialRepository credentialRepository,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuditLogService auditLogService,
                       PasswordResetTokenRepository passwordResetTokenRepository) {
        this.appUserRepository = appUserRepository;
        this.sessionRepository = sessionRepository;
        this.credentialRepository = credentialRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }


    @Transactional
    public void register(RegisterRequest request)
    {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if(appUserRepository.findByEmail(normalizedEmail).isPresent())
        {
            // why return even though the user already exists?
            // This is to prevent the user from knowing whether the email is already registered or not.
            log.debug("Registration attempted with an email that already exists");
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
            .build();
        credential = credentialRepository.save(credential);

        String rawToken = UUID.randomUUID().toString();
        
        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder()
            .userId(user.getId())
            .tokenHash(hashToken(rawToken))
            .expiresAt(now.plusHours(24))
            .build();

        emailVerificationTokenRepository.save(emailVerificationToken);
        log.info("Verification link for {}: /api/auth/verify-email?token={}", normalizedEmail, rawToken);

        // now audit loging
        auditLogService.record(user.getId(), "register", null, null);

        log.info("New user registered: {}", user.getId());
    }


    public String hashToken(String rawToken) {
        
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

    @Transactional
    public void verifyEmail(String rawToken)
    {

        // hash the raw incoming raw token
        String tokenHash = hashToken(rawToken);
        OffsetDateTime now = OffsetDateTime.now();
        // look up for the EmailVerificationToken
        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(tokenHash).orElseThrow(() -> new IllegalArgumentException("Token verification failed"));

        if(token.getExpiresAt().isBefore(now) || token.getUsedAt() != null)
        {
            auditLogService.recordIndependent(null, "email_verification_failed", null, null);
            throw new IllegalArgumentException("Token verification failed");
        }
        AppUser user = appUserRepository.findById(token.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        appUserRepository.save(user);

        token.setUsedAt(now);
        emailVerificationTokenRepository.save(token);

        auditLogService.record(user.getId(), "email_verified", null, null);
        log.info("Email verified for user {}", user.getId());
    }

    @Transactional
    public String login(LoginRequest request, String userAgent, String ipAddress)
    {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        OffsetDateTime now = OffsetDateTime.now();


        AppUser user = appUserRepository.findByEmail(normalizedEmail).orElseThrow(() -> {
            
            auditLogService.recordIndependent(null, "login_failure", ipAddress, userAgent);
            log.warn("Login failed for unknown user with email {}", normalizedEmail);
            throw new IllegalArgumentException("Login failed");
        });
        Credential credential = credentialRepository.findByUserIdAndType(user.getId(),CredentialType.PASSWORD).orElse(null);

        boolean passwordMatches = (credential != null && credential.getSecretHash()!= null && passwordEncoder.matches(request.getPassword(), credential.getSecretHash()));

        if(user == null || credential == null || !passwordMatches || user.getStatus() != UserStatus.ACTIVE || !user.getEmailVerified())
        {
            auditLogService.recordIndependent(user != null? user.getId(): null, "login_failure", ipAddress, userAgent);
            log.warn("Login failed for user {}", user != null ? user.getId() : "unknown");
            throw new IllegalArgumentException("Login failed");
        }

        credential.setLastUsedAt(now);
        credentialRepository.save(credential);

        String rawToken = UUID.randomUUID().toString();
        Session session = Session.builder()
            .userId(user.getId())
            .tokenHash(hashToken(rawToken))
            .expiresAt(now.plusDays(7))
            .userAgent(userAgent)
            .ipAddress(ipAddress)
            .build();
        sessionRepository.save(session);

        auditLogService.record(user.getId(), "login_success", ipAddress, userAgent);

        log.info("Login succeeded for user {}", user.getId());
        return rawToken;
    }

    public Optional<UUID> validateSession(String rawToken) {
        OffsetDateTime now = OffsetDateTime.now();
        return findSessionByRawToken(rawToken)
        .filter(session -> session.getRevokedAt() == null)
        .filter(session -> session.getExpiresAt().isAfter(now))
        .map(session -> session.getUserId());
    }

    private Optional<Session> findSessionByRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return sessionRepository.findByTokenHash(hashToken(rawToken));
    }
    @Transactional
    public void logout(String rawToken, UUID userId, String ipAddress, String userAgent) {
        Session session = findSessionByRawToken(rawToken)
            .orElseThrow(() -> {
                log.warn("Logout attempted with an invalid token for user {}", userId);
                auditLogService.recordIndependent(userId, "logout_failure", ipAddress, userAgent);
                throw new IllegalArgumentException("Invalid session token");
            });

        session.setRevokedAt(OffsetDateTime.now());
        sessionRepository.save(session);
        auditLogService.record(userId, "logout_success", ipAddress, userAgent);
        log.info("Logout succeeded for user {}", userId);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        OffsetDateTime now = OffsetDateTime.now();

        appUserRepository.findByEmail(normalizedEmail).ifPresent(
            user -> {
                String rawToken = UUID.randomUUID().toString();

                PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                    .userId(user.getId())
                    .tokenHash(hashToken(rawToken))
                    .expiresAt(now.plusHours(1))
                    .build();
                passwordResetTokenRepository.save(passwordResetToken);

                log.info("Password reset link for {}: /api/auth/reset-password?token={}", normalizedEmail, rawToken);
                auditLogService.record(user.getId(), "password_reset_requested", null, null);
            }
        );
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword){
        String tokenHash = hashToken(rawToken);
        OffsetDateTime now = OffsetDateTime.now();

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> {
                log.warn("Password reset attempted with an invalid token");
                auditLogService.recordIndependent(null, "password_reset_failed", null, null);
                throw new IllegalArgumentException("Invalid password reset token");
            });
        if(token.getExpiresAt().isBefore(now) || token.getUsedAt() != null)
        {
            log.warn("Password reset attempted with an expired or already used token");
            auditLogService.recordIndependent(token.getUserId(), "password_reset_failed", null, null);
            throw new IllegalArgumentException("Invalid password reset token");
        }

        Credential credential = credentialRepository.findByUserIdAndType(token.getUserId(), CredentialType.PASSWORD)
            .orElseThrow(() -> {
                log.warn("Password reset attempted for a user without a password credential");
                auditLogService.recordIndependent(token.getUserId(), "password_reset_failed", null, null);
                throw new IllegalArgumentException("User does not have a password credential");
            });

        credential.setSecretHash(passwordEncoder.encode(newPassword));
        credentialRepository.save(credential);

        token.setUsedAt(now);
        passwordResetTokenRepository.save(token);

        List<Session> activeSessions = sessionRepository.findAllByUserIdAndRevokedAtIsNull(token.getUserId());

        activeSessions.forEach(session -> session.setRevokedAt(now));
        sessionRepository.saveAll(activeSessions);

        auditLogService.record(token.getUserId(), "password_reset_success", null, null);
        log.info("Password reset successful for user {}", token.getUserId());
    }

}
