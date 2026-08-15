package com.khan.oauth2springboot.service;

import java.util.UUID;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.stereotype.Service;

import com.khan.oauth2springboot.entity.AuditLog;
import com.khan.oauth2springboot.repository.AuditLogRepository;

import org.springframework.transaction.annotation.Transactional;


@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordIndependent(UUID userId, String eventType, String ipAddress, String userAgent)
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
