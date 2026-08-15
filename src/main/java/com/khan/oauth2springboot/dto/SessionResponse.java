package com.khan.oauth2springboot.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionResponse(UUID id, String userAgent, String ipAddress, OffsetDateTime createdAt, OffsetDateTime expiresAt) {
}
