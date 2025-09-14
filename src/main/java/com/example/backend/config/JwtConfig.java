package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Bean
    public SecretKey jwtSecretKey() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty() || jwtSecret.equals("mySecretKey")) {
            // Generate a secure random key for development
            try {
                KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
                keyGen.init(256, new SecureRandom());
                SecretKey secretKey = keyGen.generateKey();
                String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
                System.out.println("Generated JWT Secret Key (base64): " + encodedKey);
                System.out.println("⚠️  Please set JWT_SECRET environment variable in production!");
                return secretKey;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Error generating JWT secret key", e);
            }
        }

        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public Long getJwtExpiration() {
        return jwtExpiration;
    }

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}