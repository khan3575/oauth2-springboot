package com.khan.oauth2springboot.dto;

import java.util.UUID;

import com.khan.oauth2springboot.entity.enums.UserStatus;

public record MeResponse(UUID id, String email, boolean emailVerified, UserStatus status) {
}
