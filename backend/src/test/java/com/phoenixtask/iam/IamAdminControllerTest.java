package com.phoenixtask.iam;

import com.phoenixtask.iam.model.User;
import com.phoenixtask.iam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
public class IamAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("admin", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void listUsers_Success() throws Exception {
        authenticate("platform_owner");
        User user = new User(1L, "user@test.com", "User One", "hash", "ACTIVE", false, false, null, null, null);
        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/iam/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user@test.com"));
    }

    /*
    @Test
    public void listUsers_ForbiddenForUser() throws Exception {
        authenticate("USER");
        mockMvc.perform(get("/api/iam/users"))
                .andExpect(status().isForbidden());
    }
    */

    @Test
    public void getUserDetail_Success() throws Exception {
        authenticate("platform_owner");
        User user = new User(1L, "user@test.com", "User One", "hash", "ACTIVE", false, false, null, null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findRolesByUserId(1L)).thenReturn(List.of("DEVELOPER"));

        mockMvc.perform(get("/api/iam/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.roles[0]").value("DEVELOPER"));
    }

    @Test
    public void updateUserStatus_Success() throws Exception {
        authenticate("platform_owner");
        User user = new User(1L, "user@test.com", "User One", "hash", "ACTIVE", false, false, null, null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(patch("/api/iam/users/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"INACTIVE\"}"))
                .andExpect(status().isOk());

        verify(userRepository).save(any(User.class));
    }

    @Test
    public void addRole_Success() throws Exception {
        authenticate("platform_owner");
        mockMvc.perform(post("/api/iam/users/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"DEVELOPER\"}"))
                .andExpect(status().isOk());

        verify(userRepository).addRole(eq(1L), eq("DEVELOPER"));
    }

    @Test
    public void removeRole_Success() throws Exception {
        authenticate("platform_owner");
        mockMvc.perform(delete("/api/iam/users/1/roles/DEVELOPER"))
                .andExpect(status().isOk());

        verify(userRepository).removeRole(eq(1L), eq("DEVELOPER"));
    }
}
