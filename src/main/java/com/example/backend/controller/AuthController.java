package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UserDto;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration attempt for username: {}", request.getUsername());

        UserDto user = authService.register(request);

        logger.info("User registered successfully: {}", user.getUsername());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login attempt for username: {}", request.getUsername());

        LoginResponse response = authService.login(request);

        logger.info("User logged in successfully: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        logger.info("Token refresh attempt");

        LoginResponse response = authService.refreshToken(refreshToken);

        logger.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // In a stateless JWT implementation, logout is typically handled on the client side
        // by removing the token from storage. For enhanced security, you could maintain
        // a blacklist of tokens on the server side.

        logger.info("Logout endpoint called");

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "auth-service",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}