package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.service.AuthService;
import com.example.backend.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import com.example.backend.dto.UserDto;
import com.example.backend.dto.LoginResponse;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest(
                "testuser",
                "test@example.com",
                "password123",
                "Test",
                "User"
        );

        validLoginRequest = new LoginRequest("testuser", "password123");
        
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new UserDto(1L, validRegisterRequest.getUsername(), validRegisterRequest.getEmail(), validRegisterRequest.getFirstName(), validRegisterRequest.getLastName(), true, LocalDateTime.now(), LocalDateTime.now(), Set.of("USER")));

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("access-token", "refresh-token", 1L, validLoginRequest.getUsername(), "test@example.com", Set.of("USER")));
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        mockMvc.perform(post("/auth/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRegisterRequest)))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectInvalidRegistrationData() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "", "", "", "");

        mockMvc.perform(post("/auth/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginUserSuccessfully() throws Exception {
        mockMvc.perform(post("/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validLoginRequest)))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidLoginData() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("", "");

        mockMvc.perform(post("/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }
}