package com.khan.oauth2springboot.config;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.khan.oauth2springboot.service.AuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {
    private final AuthService authService;
    private final CookieUtils cookieUtils;
    public SessionAuthenticationFilter(AuthService authService, CookieUtils cookieUtils) {
        this.authService = authService;
        this.cookieUtils = cookieUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request
        , HttpServletResponse response
        , FilterChain filterChain) throws ServletException, IOException 
    {

        String rawToken = cookieUtils.extractSessionCookie(request);

        if(rawToken != null && !rawToken.isBlank())
        {
            Optional<UUID> userId = authService.validateSession(rawToken);
            userId.ifPresent(id ->{
                var authentication = new UsernamePasswordAuthenticationToken(id, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        filterChain.doFilter(request, response);
    }
    
    
    

}
