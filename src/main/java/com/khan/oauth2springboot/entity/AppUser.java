package com.khan.oauth2springboot.entity;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.khan.oauth2springboot.entity.enums.UserStatus;

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

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="app_user")
public class AppUser{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id", unique=true)
    private UUID id;
   
    @Column(name="email", unique=true, nullable=false)
    private String email;

    @Column(name="email_verified", nullable=false)
    private Boolean emailVerified;

    @Column(name="status", nullable=false)
    private UserStatus status;

    @Builder.Default
    @Column(name="failed_login_attempts", nullable=false)
    private Integer failedLoginAttempts = 0;

    @Column(name="locked_until")
    private OffsetDateTime lockedUntil;

    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)  
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;



}