package com.khan.oauth2springboot.exception;

import java.util.Map;

public record ErrorResponse(String error, String message, Map<String, String> fieldErrors) {
    
}
