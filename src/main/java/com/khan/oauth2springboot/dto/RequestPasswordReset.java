package com.khan.oauth2springboot.controller;

import lombok.Getter;
import lombok.Setter;

/**
 * RequestPasswordReset
 */
@Getter
@Setter
public class RequestPasswordReset {

    private String token;
    private String newPassword;
}
