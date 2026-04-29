package com.phoenixtask.projects;

import com.phoenixtask.projects.model.Project;
import com.phoenixtask.projects.repository.ProjectRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    private void authenticate(String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user@test.com", "password",
                authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void listProjects_Success() throws Exception {
        authenticate("viewer");
        Project project = new Project(1L, "PRJ-1", "Project One", "Desc", "ACTIVE", 1L, null, null, LocalDateTime.now(),
                null);
        when(projectService.getAllProjects()).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectKey").value("PRJ-1"))
                .andExpect(jsonPath("$[0].name").value("Project One"));
    }

    @Test
    public void getProject_Success() throws Exception {
        authenticate("viewer");
        Project project = new Project(1L, "PRJ-1", "Project One", "Desc", "ACTIVE", 1L, null, null, LocalDateTime.now(),
                null);
        when(projectService.getProjectById(1L)).thenReturn(Optional.of(project));

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectKey").value("PRJ-1"));
    }

    @Test
    public void createProject_Success() throws Exception {
        authenticate("manager");
        Project project = new Project(1L, "NEW-1", "New Project", "Desc", "DRAFT", 1L, null, null, LocalDateTime.now(),
                null);
        when(projectService.createProject(any())).thenReturn(project);

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectKey\":\"NEW-1\", \"name\":\"New Project\", \"ownerUserId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectKey").value("NEW-1"));
    }

    @Test
    public void updateStatus_Success() throws Exception {
        authenticate("platform_owner");
        mockMvc.perform(patch("/api/projects/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void updateStatus_ProjectNotFound_Returns404() throws Exception {
        authenticate("platform_owner");
        doThrow(new com.phoenixtask.shared.error.ResourceNotFoundException("Project not found"))
                .when(projectService).updateProjectStatus(eq(999L), anyString());

        mockMvc.perform(patch("/api/projects/999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Project not found"));
    }
}
