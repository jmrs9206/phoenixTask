package com.phoenixtask.kanban;

import com.phoenixtask.kanban.KanbanController.MoveRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
public class KanbanSecurityEnforcementTest {

    @Autowired
    private KanbanController kanbanController;

    @MockitoBean
    private KanbanService kanbanService;

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
    public void moveIssue_ThrowsAccessDenied_WhenViewerRole() {
        authenticate("viewer");
        MoveRequest request = new MoveRequest();
        request.setProjectId(1L);
        request.setTargetStatus("DONE");
        request.setTargetIndex(0);

        assertThrows(AccessDeniedException.class, () -> {
            kanbanController.moveIssue(1L, request);
        });
    }

    @Test
    public void moveIssue_Succeeds_WhenManagerRole() {
        authenticate("manager");
        MoveRequest request = new MoveRequest();
        request.setProjectId(1L);
        request.setTargetStatus("DONE");
        request.setTargetIndex(0);

        kanbanController.moveIssue(1L, request);
    }

    @Test
    public void moveIssue_Succeeds_WhenQaRole() {
        authenticate("qa");
        MoveRequest request = new MoveRequest();
        request.setProjectId(1L);
        request.setTargetStatus("IN_REVIEW");
        request.setTargetIndex(0);

        kanbanController.moveIssue(1L, request);
    }
}
