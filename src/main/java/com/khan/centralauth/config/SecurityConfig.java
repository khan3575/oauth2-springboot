package com.khan.centralauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password4j.Argon2Password4jPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.password4j.Argon2Function;
import com.password4j.types.Argon2;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2Password4jPasswordEncoder(
            Argon2Function.getInstance(19456,2, 1, 32, Argon2.ID)
        );
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SessionAuthenticationFilter sessionAuthenticationFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(
                auth -> auth
                    .requestMatchers("/api/auth/register"
                    , "/api/auth/login"
                    , "/api/auth/verify-email"
                    , "/error"
                    , "/v3/api-docs/**"
                    , "/swagger-ui/**"
                    , "/swagger-ui.html").permitAll()
                    .requestMatchers("/api/auth/logout").authenticated()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    
}
