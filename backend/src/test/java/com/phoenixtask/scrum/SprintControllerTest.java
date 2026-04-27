package com.phoenixtask.scrum;

import com.phoenixtask.scrum.model.Sprint;
import com.phoenixtask.scrum.repository.SprintRepository;
import com.phoenixtask.projects.repository.ProjectRepository;
import com.phoenixtask.issues.repository.IssueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
public class SprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SprintService sprintService;

    @MockitoBean
    private SprintRepository sprintRepository;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private IssueRepository issueRepository;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    private void authenticate(String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user@test.com", "password", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void listSprints_Success() throws Exception {
        authenticate("viewer");
        Sprint sprint = new Sprint();
        sprint.setId(1L);
        sprint.setName("Sprint 1");
        
        when(sprintService.getSprintsByProject(1L)).thenReturn(List.of(sprint));

        mockMvc.perform(get("/api/scrum/sprints?projectId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sprint 1"));
    }

    @Test
    public void createSprint_Success() throws Exception {
        authenticate("manager");
        Sprint sprint = new Sprint();
        sprint.setId(1L);
        sprint.setName("New Sprint");
        
        when(sprintService.createSprint(any())).thenReturn(sprint);

        mockMvc.perform(post("/api/scrum/sprints")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectId\":1, \"name\":\"New Sprint\", \"startDate\":\"2024-01-01\", \"endDate\":\"2024-01-15\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Sprint"));
    }

    @Test
    public void startSprint_Success() throws Exception {
        authenticate("platform_owner");
        doNothing().when(sprintService).startSprint(1L);

        mockMvc.perform(post("/api/scrum/sprints/1/start"))
                .andExpect(status().isOk());
    }

    @Test
    public void assignIssue_Success() throws Exception {
        authenticate("manager");
        doNothing().when(sprintService).assignIssue(1L, 10L);

        mockMvc.perform(post("/api/scrum/sprints/1/issues/10"))
                .andExpect(status().isOk());
    }
}
