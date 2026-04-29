package com.phoenixtask.issues;

import com.phoenixtask.issues.model.Issue;
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
public class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IssueService issueService;

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
    public void getIssues_Success() throws Exception {
        authenticate("viewer");
        Issue issue = new Issue();
        issue.setId(1L);
        issue.setIssueKey("PRJ-1");
        issue.setTitle("Test Issue");
        
        when(issueService.getIssuesByProject(1L)).thenReturn(List.of(issue));

        mockMvc.perform(get("/api/issues?projectId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].issueKey").value("PRJ-1"))
                .andExpect(jsonPath("$[0].title").value("Test Issue"));
    }

    @Test
    public void getIssue_Success() throws Exception {
        authenticate("viewer");
        Issue issue = new Issue();
        issue.setId(1L);
        issue.setIssueKey("PRJ-1");
        
        when(issueService.getIssueById(1L)).thenReturn(issue);

        mockMvc.perform(get("/api/issues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issueKey").value("PRJ-1"));
    }

    @Test
    public void createIssue_Success() throws Exception {
        authenticate("manager");
        Issue issue = new Issue();
        issue.setId(1L);
        issue.setIssueKey("PRJ-1");
        issue.setTitle("New Issue");
        
        when(issueService.createIssue(any())).thenReturn(issue);

        mockMvc.perform(post("/api/issues")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectId\":1, \"title\":\"New Issue\", \"reporterUserId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issueKey").value("PRJ-1"));
    }

    @Test
    public void updateStatus_Success() throws Exception {
        authenticate("platform_owner");
        doNothing().when(issueService).updateStatus(eq(1L), eq("IN_PROGRESS"));

        mockMvc.perform(patch("/api/issues/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void updateIssue_AssigneeNotFound_Returns404() throws Exception {
        authenticate("manager");
        when(issueService.updateIssue(eq(1L), any()))
                .thenThrow(new com.phoenixtask.shared.error.ResourceNotFoundException("Assignee user not found"));

        mockMvc.perform(patch("/api/issues/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated Title\", \"assigneeUserId\":999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Assignee user not found"));
    }
}
