package com.khan.centralauth.entity;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name="app_user")
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
    private String status;

    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)  
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

}