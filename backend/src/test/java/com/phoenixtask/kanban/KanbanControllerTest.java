package com.phoenixtask.kanban;

import com.phoenixtask.kanban.model.KanbanBoard;
import com.phoenixtask.kanban.model.KanbanColumn;
import com.phoenixtask.kanban.model.KanbanIssueCard;
import com.phoenixtask.kanban.repository.KanbanRepository;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
public class KanbanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KanbanService kanbanService;

    @MockitoBean
    private KanbanRepository kanbanRepository;

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
    public void getBoard_Success() throws Exception {
        authenticate("viewer");
        KanbanBoard board = new KanbanBoard();
        board.setProjectId(1L);
        board.setColumns(List.of(new KanbanColumn("BACKLOG", List.of())));

        when(kanbanService.getBoard(1L, null)).thenReturn(board);

        mockMvc.perform(get("/api/kanban/board?projectId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1));
    }

    @Test
    public void getBoard_WithSprint_Success() throws Exception {
        authenticate("viewer");
        KanbanBoard board = new KanbanBoard();
        board.setProjectId(1L);
        board.setSprintId(2L);

        when(kanbanService.getBoard(1L, 2L)).thenReturn(board);

        mockMvc.perform(get("/api/kanban/board?projectId=1&sprintId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sprintId").value(2));
    }

    @Test
    public void moveIssue_Manager_Success() throws Exception {
        authenticate("manager");
        KanbanIssueCard card = new KanbanIssueCard();
        card.setId(1L);
        card.setStatus("IN_PROGRESS");

        when(kanbanService.moveIssue(anyLong(), anyLong(), anyString(), anyInt())).thenReturn(card);

        mockMvc.perform(patch("/api/kanban/issues/1/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectId\":1, \"targetStatus\":\"IN_PROGRESS\", \"targetIndex\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    public void moveIssue_Developer_Success() throws Exception {
        authenticate("developer");
        KanbanIssueCard card = new KanbanIssueCard();
        card.setId(1L);
        card.setStatus("DONE");

        when(kanbanService.moveIssue(anyLong(), anyLong(), anyString(), anyInt())).thenReturn(card);

        mockMvc.perform(patch("/api/kanban/issues/1/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectId\":1, \"targetStatus\":\"DONE\", \"targetIndex\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    public void getBoard_InvalidSprint_BadRequest() throws Exception {
        authenticate("viewer");

        mockMvc.perform(get("/api/kanban/board?projectId=1&sprintId=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details[?(@.field == 'getBoard.sprintId')]").exists());
    }

    @Test
    public void getBoard_ProjectNotFound_Returns404() throws Exception {
        authenticate("viewer");
        when(kanbanService.getBoard(eq(999L), any()))
                .thenThrow(new com.phoenixtask.shared.error.ResourceNotFoundException("Project not found"));

        mockMvc.perform(get("/api/kanban/board?projectId=999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Project not found"));
    }
}
