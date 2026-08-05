package com.khan.centralauth.service;

import java.util.UUID;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.stereotype.Service;

import com.khan.centralauth.entity.AuditLog;
import com.khan.centralauth.repository.AuditLogRepository;

import org.springframework.transaction.annotation.Transactional;


@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID userId, String eventType, String ipAddress, String userAgent)
    {
        AuditLog auditLog = AuditLog.builder()
            .userId(userId)
            .eventType(eventType)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
        auditLogRepository.save(auditLog);
    }
    
}
