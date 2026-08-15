package com.khan.oauth2springboot.service;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.khan.oauth2springboot.entity.AppUser;
import com.khan.oauth2springboot.entity.Credential;
import com.khan.oauth2springboot.entity.enums.CredentialType;
import com.khan.oauth2springboot.entity.enums.UserStatus;
import com.khan.oauth2springboot.repository.AppUserRepository;
import com.khan.oauth2springboot.repository.CredentialRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final CredentialRepository credentialRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email.trim().toLowerCase();

        AppUser user = appUserRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        Credential credential = credentialRepository.findByUserIdAndType(user.getId(), CredentialType.PASSWORD)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        boolean enabled = user.getStatus() == UserStatus.ACTIVE && user.getEmailVerified();

        return User.withUsername(user.getEmail())
            .password(credential.getSecretHash())
            .disabled(!enabled)
            .authorities(Collections.emptyList())
            .build();
    }
}
