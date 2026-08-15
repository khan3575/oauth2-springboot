package com.khan.oauth2springboot.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    private static final String SERVICE_METHODS = "execution(* com.khan.oauth2springboot.service..*(..))";

    @Before(SERVICE_METHODS)
    public void logMethodEntry(JoinPoint joinPoint) {
        log.debug("Entering {}.{}()",
            joinPoint.getSignature().getDeclaringType().getSimpleName(),
            joinPoint.getSignature().getName());
    }

    @AfterReturning(SERVICE_METHODS)
    public void logMethodExit(JoinPoint joinPoint) {
        log.debug("Exiting {}.{}()",
            joinPoint.getSignature().getDeclaringType().getSimpleName(),
            joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = SERVICE_METHODS, throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Throwable exception) {
        log.warn("Exception in {}.{}(): {}",
            joinPoint.getSignature().getDeclaringType().getSimpleName(),
            joinPoint.getSignature().getName(),
            exception.getMessage());
    }
}
