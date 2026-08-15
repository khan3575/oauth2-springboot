package com.khan.oauth2springboot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.khan.oauth2springboot.dto.RegisterRequest;
import com.khan.oauth2springboot.entity.AppUser;
import com.khan.oauth2springboot.entity.Credential;
import com.khan.oauth2springboot.entity.EmailVerificationToken;
import com.khan.oauth2springboot.entity.enums.CredentialType;
import com.khan.oauth2springboot.entity.enums.UserStatus;
import com.khan.oauth2springboot.repository.AppUserRepository;
import com.khan.oauth2springboot.repository.CredentialRepository;
import com.khan.oauth2springboot.repository.EmailVerificationTokenRepository;
import com.khan.oauth2springboot.repository.PasswordResetTokenRepository;
import com.khan.oauth2springboot.repository.SessionRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private CredentialRepository credentialRepository;
    @Mock private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditLogService auditLogService;
    @Mock private EmailService emailService;

    @InjectMocks private AuthService authService;


    @Test
    public void register_newEmail_createsUserCredentialAndVerificationToken(){
        RegisterRequest request = new RegisterRequest();

        request.setEmail("sakib@gmail.com");
        request.setPassword("pass@12A/");

        when(appUserRepository.findByEmail("sakib@gmail.com")).thenReturn(Optional.empty());


        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(credentialRepository.save(any(Credential.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("pass@12A/")).thenReturn("hashed-password");


        authService.register(request);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());
        AppUser savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("sakib@gmail.com");
        assertThat(savedUser.getEmailVerified()).isFalse();


        ArgumentCaptor<Credential> credentialCaptor = ArgumentCaptor.forClass(Credential.class);
        verify(credentialRepository).save(credentialCaptor.capture());
        Credential savedCredential = credentialCaptor.getValue();

        assertThat(savedCredential.getType()).isEqualTo(CredentialType.PASSWORD);
        assertThat(savedCredential.getSecretHash()).isEqualTo("hashed-password");

        verify(emailVerificationTokenRepository).save(any());
        verify(auditLogService).record(any(), eq("register"), isNull(), isNull());

    }

    @Test
    public void register_duplicateEmail_doesNothing()
    {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("sakib@gmail.com");
        request.setPassword("correctPassword");

        when(appUserRepository.findByEmail("sakib@gmail.com"))
                .thenReturn(Optional.of(AppUser.builder().email("sakib@gmail.com").build()));
        
        authService.register(request);

        verify(appUserRepository, never()).save(any());
        verify(credentialRepository, never()).save(any());
        verify(emailVerificationTokenRepository, never()).save(any());
        verify(auditLogService, never()).record(any(), any(), any(), any());
    }

    @Test
    public void verifyEmail_validToken_activatesUser() {
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        EmailVerificationToken token = EmailVerificationToken.builder()
            .userId(userId)
            .tokenHash("hashed-token")
            .expiresAt(now.plusHours(1))
            .build();

        AppUser user = AppUser.builder()
            .id(userId)
            .email("sakib@gmail.com")
            .emailVerified(false)
            .status(UserStatus.PENDING_VERIFICATION)
            .build();

        when(emailVerificationTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.verifyEmail("raw-token");

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmailVerified()).isTrue();
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.ACTIVE);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(emailVerificationTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getUsedAt()).isNotNull();

        verify(auditLogService).record(eq(userId), eq("email_verified"), isNull(), isNull());
    }

    @Test
    public void verifyEmail_expiredToken_throws() {
        OffsetDateTime now = OffsetDateTime.now();
        EmailVerificationToken token = EmailVerificationToken.builder()
            .userId(UUID.randomUUID())
            .tokenHash("hashed-token")
            .expiresAt(now.minusHours(1))
            .build();

        when(emailVerificationTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> authService.verifyEmail("raw-token"));

        verify(auditLogService).recordIndependent(isNull(), eq("email_verification_failed"), isNull(), isNull());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    public void verifyEmail_alreadyUsedToken_throws() {
        OffsetDateTime now = OffsetDateTime.now();
        EmailVerificationToken token = EmailVerificationToken.builder()
            .userId(UUID.randomUUID())
            .tokenHash("hashed-token")
            .expiresAt(now.plusHours(1))
            .usedAt(now.minusMinutes(5))
            .build();

        when(emailVerificationTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> authService.verifyEmail("raw-token"));

        verify(auditLogService).recordIndependent(isNull(), eq("email_verification_failed"), isNull(), isNull());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    public void verifyEmail_unknownToken_throws() {
        when(emailVerificationTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.verifyEmail("raw-token"));

        verify(auditLogService, never()).recordIndependent(any(), any(), any(), any());
        verify(appUserRepository, never()).findById(any());
    }




}
