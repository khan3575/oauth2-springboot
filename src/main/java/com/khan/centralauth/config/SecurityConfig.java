package com.khan.centralauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password4j.Argon2Password4jPasswordEncoder;

import com.password4j.Argon2Function;
import com.password4j.types.Argon2;


@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2Password4jPasswordEncoder(
            Argon2Function.getInstance(19456,2, 1, 32, Argon2.ID)
        );
    }

    
}
