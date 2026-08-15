package com.khan.oauth2springboot.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password4j.Argon2Password4jPasswordEncoder;

import com.password4j.Argon2Function;
import com.password4j.types.Argon2;

public class SecurityConfigTest {
    private final PasswordEncoder encoder = new Argon2Password4jPasswordEncoder(
        Argon2Function.getInstance(19456, 2, 1, 32, Argon2.ID)
    );

    @Test
    void hashesAreVerifiable(){
        String hash1 = encoder.encode("password");
        String hash2 = encoder.encode("password");

        assertNotEquals(hash1, hash2); 
        assertTrue(encoder.matches("password", hash1));
        assertFalse(encoder.matches("!password", hash1));
    }
}
