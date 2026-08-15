package com.khan.oauth2springboot.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.khan.oauth2springboot.entity.Credential;
import com.khan.oauth2springboot.entity.enums.CredentialType;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, UUID> {
    Optional<Credential> findByUserIdAndType(UUID userId, CredentialType type);
}
