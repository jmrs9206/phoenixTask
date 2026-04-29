package com.phoenixtask.projects;

import com.phoenixtask.projects.model.Project;
import com.phoenixtask.projects.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
public class ProjectSecurityEnforcementTest {

    @Autowired
    private ProjectController projectController;

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
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", "pass", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void createProject_ThrowsException_WhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            projectController.createProject(new ProjectRequests.CreateProjectRequest("PROJ", "Name", "Desc", 1L, null, null));
        });
    }

    @Test
    public void createProject_ThrowsException_WhenRoleInsufficient() {
        authenticate("viewer");
        assertThrows(AccessDeniedException.class, () -> {
            projectController.createProject(new ProjectRequests.CreateProjectRequest("PROJ", "Name", "Desc", 1L, null, null));
        });
    }

    @Test
    public void createProject_Succeeds_WhenManagerRolePresent() {
        authenticate("manager");
        projectController.createProject(new ProjectRequests.CreateProjectRequest("PROJ", "Name", "Desc", 1L, null, null));
    }

    @Test
    public void updateStatus_Succeeds_WhenPlatformOwnerRolePresent() {
        authenticate("platform_owner");
        projectController.updateStatus(1L, new ProjectRequests.UpdateStatusRequest("ACTIVE"));
    }
}
