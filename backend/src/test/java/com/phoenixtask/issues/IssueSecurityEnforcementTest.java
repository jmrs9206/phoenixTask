package com.phoenixtask.issues;

import com.phoenixtask.issues.repository.IssueRepository;
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

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
public class IssueSecurityEnforcementTest {

    @Autowired
    private IssueController issueController;

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
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", "pass", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void createIssue_ThrowsException_WhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            issueController.createIssue(
                    new IssueRequests.CreateIssueRequest(1L, "Title", "Desc", 1L, null, "BACKLOG", "LOW", null, null));
        });
    }

    @Test
    public void createIssue_ThrowsException_WhenRoleInsufficient() {
        authenticate("viewer");
        assertThrows(AccessDeniedException.class, () -> {
            issueController.createIssue(
                    new IssueRequests.CreateIssueRequest(1L, "Title", "Desc", 1L, null, "BACKLOG", "LOW", null, null));
        });
    }

    @Test
    public void createIssue_Succeeds_WhenManagerRolePresent() {
        authenticate("manager");
        IssueRequests.CreateIssueRequest request = new IssueRequests.CreateIssueRequest(
                1L, "Title", "Desc", 1L, null, "BACKLOG", "LOW", null, null);
        issueController.createIssue(request);
    }

    @Test
    public void updateStatus_Succeeds_WhenPlatformOwnerRolePresent() {
        authenticate("platform_owner");
        issueController.updateStatus(1L, new IssueRequests.UpdateStatusRequest("IN_PROGRESS"));
    }
}
