package com.khan.oauth2springboot.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.khan.oauth2springboot.entity.enums.CredentialType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="credential")
public class Credential {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id")
    private UUID id;

    @Column(name="user_id", nullable = false)
    private UUID userId;

    @Column(name="type", nullable = false)
    private CredentialType type;

    @Column(name="secret_hash", nullable = true)
    private String secretHash;

    @CreationTimestamp
    @Column(name="created_at", nullable = false)
    private OffsetDateTime createdAt;



    @Column(name="last_used_at", nullable = true)
    private OffsetDateTime lastUsedAt;
}
