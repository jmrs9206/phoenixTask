package com.phoenixtask.scrum;

import com.phoenixtask.scrum.model.Sprint;
import com.phoenixtask.scrum.repository.SprintRepository;
import com.phoenixtask.projects.repository.ProjectRepository;
import com.phoenixtask.issues.repository.IssueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
public class SprintSecurityEnforcementTest {

    @Autowired
    private SprintController sprintController;

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
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", "pass", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void createSprint_ThrowsException_WhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            sprintController.create(new SprintRequests.CreateSprintRequest(1L, "Sprint 1", null, null, null, "PLANNED"));
        });
    }

    @Test
    public void createSprint_ThrowsException_WhenRoleInsufficient() {
        authenticate("viewer");
        assertThrows(AccessDeniedException.class, () -> {
            sprintController.create(new SprintRequests.CreateSprintRequest(1L, "Sprint 1", null, null, null, "PLANNED"));
        });
    }

    @Test
    public void createSprint_Succeeds_WhenManagerRolePresent() {
        authenticate("manager");
        sprintController.create(new SprintRequests.CreateSprintRequest(1L, "Sprint 1", null, null, null, "PLANNED"));
    }

    @Test
    public void startSprint_Succeeds_WhenPlatformOwnerRolePresent() {
        authenticate("platform_owner");
        sprintController.start(1L);
    }

    @Test
    public void assignIssue_Succeeds_WhenManagerRolePresent() {
        authenticate("manager");
        sprintController.assignIssue(1L, 10L);
    }
}
