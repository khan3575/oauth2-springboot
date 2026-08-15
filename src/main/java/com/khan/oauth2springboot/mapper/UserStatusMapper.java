package com.khan.oauth2springboot.mapper;

import com.khan.oauth2springboot.entity.enums.UserStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusMapper implements AttributeConverter<UserStatus,String> {
    
    public String convertToDatabaseColumn(UserStatus status)
    {
        return status == null ? null : status.name().toLowerCase();
    }

    public UserStatus convertToEntityAttribute(String status)
    {
        return status == null ? null : UserStatus.valueOf(status.toUpperCase());
    }
}
