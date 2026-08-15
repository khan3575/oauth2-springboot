package com.khan.oauth2springboot.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password4j.Argon2Password4jPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins:}") String allowedOrigins) {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toList();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SessionAuthenticationFilter sessionAuthenticationFilter) throws Exception {
        http.cors(Customizer.withDefaults())
            // The JSON API under /api/** doesn't rely on ambient browser auth the way
            // classic session cookies do for CSRF purposes (SESSION cookie is SameSite=Lax
            // and validated server-side as an opaque token, not used implicitly by forms),
            // and requiring a CSRF token there would be a breaking API contract change for
            // any client. Keep CSRF enabled for /login and the OAuth2 authorization endpoints,
            // where formLogin() actually renders browser-submitted forms.
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(
                auth -> auth
                    .requestMatchers("/api/auth/register"
                    , "/api/auth/login"
                    , "/api/auth/verify-email"
                    ,"/api/auth/forgot-password"
                    ,"/api/auth/reset-password"
                    , "/login"
                    , "/error"
                    , "/actuator/health"
                    , "/v3/api-docs/**"
                    , "/swagger-ui/**"
                    , "/swagger-ui.html").permitAll()
                    .requestMatchers("/api/auth/logout").authenticated()
                    .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .exceptionHandling(exceptions -> {
                MediaTypeRequestMatcher htmlMatcher = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
                htmlMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
                exceptions
                    .defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        htmlMatcher
                    )
                    .defaultAuthenticationEntryPointFor(
                        new Http403ForbiddenEntryPoint(),
                        AnyRequestMatcher.INSTANCE
                    );
            })
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    
}
