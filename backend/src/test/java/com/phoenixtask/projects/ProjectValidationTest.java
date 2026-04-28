package com.phoenixtask.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc
public class ProjectValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "manager")
    public void testCreateProjectValidation() throws Exception {
        // Test short name and invalid key
        ProjectRequests.CreateProjectRequest request = new ProjectRequests.CreateProjectRequest(
                "invalid-key", // Not uppercase alphanumeric (min 2, max 10, pattern)
                "A", // Name
                "Description",
                1L, // ownerUserId
                null, // plannedStartDate
                null  // plannedEndDate
        );

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details[?(@.field == 'projectKey')]").exists());
    }

    @Test
    @WithMockUser(authorities = "manager")
    public void testCreateProjectEmptyName() throws Exception {
        ProjectRequests.CreateProjectRequest request = new ProjectRequests.CreateProjectRequest(
                "PHX", 
                "", // Empty name
                "Description",
                1L,
                null,
                null
        );

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[?(@.field == 'name')]").exists());
    }
}
