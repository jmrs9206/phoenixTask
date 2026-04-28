package com.phoenixtask.auth;

import com.phoenixtask.iam.model.User;
import com.phoenixtask.iam.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc
public class SecurityMockFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JdbcTemplate jdbcTemplate; // Required to prevent context load failure due to Flyway exclusions

    @Test
    public void testFullSecurityFlow() throws Exception {
        // 1. Mock user and roles
        User mockUser = new User(1L, "test@test.com", "Test User", 
                passwordEncoder.encode("password"), "ACTIVE", false, false, 
                null, null, null);
        
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.findRolesByUserId(1L)).thenReturn(List.of("DEVELOPER"));

        // 2. Perform Login
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        String token = loginResponse.split("\"")[3]; // Simple extraction of "token" value

        // 3. Access /me with Token
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.roles[0]").value("DEVELOPER"));

        // 4. Access /me without Token -> Should be 403 Forbidden (Spring Security rejects it before /me logic)
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }
}
