package com.khan.oauth2springboot.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.khan.oauth2springboot.entity.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByTokenHash(String tokenHash);
    List<Session> findAllByUserIdAndRevokedAtIsNull(UUID userId);
}
