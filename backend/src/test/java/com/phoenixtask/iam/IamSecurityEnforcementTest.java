package com.phoenixtask.iam;

import com.phoenixtask.iam.repository.UserRepository;
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
public class IamSecurityEnforcementTest {

    @Autowired
    private IamController iamController;

    @MockitoBean
    private IamService iamService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    public void listUsers_ThrowsException_WhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
            iamController.listUsers();
        });
    }

    @Test
    public void listUsers_ThrowsException_WhenNoPlatformOwnerRole() {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", "pass", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(AccessDeniedException.class, () -> {
            iamController.listUsers();
        });
    }

    @Test
    public void listUsers_Succeeds_WhenPlatformOwnerRolePresent() {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("platform_owner"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("admin", "pass", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Should not throw exception
        iamController.listUsers();
    }
}
