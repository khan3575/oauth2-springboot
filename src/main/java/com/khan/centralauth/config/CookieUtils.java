package com.khan.centralauth.config;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CookieUtils {
    public String extractSessionCookie(HttpServletRequest request)
    {
        if(request.getCookies() == null)
        {
            return null;
        }
        for(Cookie cookie: request.getCookies())
        {
            if("SESSION".equals(cookie.getName()))
            {
                return cookie.getValue();
            }
        }
        return null;
    }
}
