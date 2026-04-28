package com.phoenixtask.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenixtask.auth.controller.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.phoenixtask.shared.error.GlobalExceptionHandler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthValidationTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(null))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testLoginValidation() throws Exception {
        // Invalid email format and empty password
        AuthController.LoginRequest request = new AuthController.LoginRequest(
                "invalid-email",
                ""
        );

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[?(@.field == 'email')]").exists())
                .andExpect(jsonPath("$.details[?(@.field == 'password')]").exists());
    }

    @Test
    public void testInviteValidation() throws Exception {
        // Missing required fields for InviteRequest (email is null)
        AuthController.InviteRequest request = new AuthController.InviteRequest(
                "invalid-email",
                1L
        );

        mockMvc.perform(post("/api/auth/invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[?(@.field == 'email')]").exists());
    }
}
