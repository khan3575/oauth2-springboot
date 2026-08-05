package com.khan.centralauth.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.khan.centralauth.entity.AuditLog;


@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    // Additional query methods can be defined here if needed
    
}
