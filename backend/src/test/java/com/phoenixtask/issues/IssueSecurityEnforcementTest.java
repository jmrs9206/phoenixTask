package com.phoenixtask.issues;

import com.phoenixtask.issues.model.Issue;
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
import java.util.Map;

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

    @Test
    public void createIssue_ThrowsException_WhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            issueController.createIssue(null);
        });
    }

    @Test
    public void createIssue_ThrowsException_WhenRoleInsufficient() {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("viewer"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", "pass", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(AccessDeniedException.class, () -> {
            issueController.createIssue(null);
        });
    }

    @Test
    public void createIssue_Succeeds_WhenManagerRolePresent() {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("manager"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("admin", "pass", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        issueController.createIssue(new Issue());
    }

    @Test
    public void updateStatus_Succeeds_WhenPlatformOwnerRolePresent() {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("platform_owner"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("admin", "pass", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        issueController.updateStatus(1L, Map.of("status", "IN_PROGRESS"));
    }
}
