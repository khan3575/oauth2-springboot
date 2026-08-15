package com.khan.oauth2springboot.mapper;

import com.khan.oauth2springboot.entity.enums.CredentialType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CredentialTypeMapper implements AttributeConverter<CredentialType, String> {

    @Override
    public String convertToDatabaseColumn(CredentialType attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public CredentialType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : CredentialType.valueOf(dbData.toUpperCase());
    }
    
}
