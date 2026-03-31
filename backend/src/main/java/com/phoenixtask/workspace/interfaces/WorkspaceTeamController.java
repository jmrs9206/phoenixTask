package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.workspace.application.WorkspaceTeamService;
import com.phoenixtask.workspace.application.WorkspaceTeamManagementService;
import com.phoenixtask.workspace.application.dto.MemberAddRequest;
import com.phoenixtask.workspace.application.dto.MemberResponse;
import com.phoenixtask.workspace.application.dto.TeamCreateRequest;
import com.phoenixtask.workspace.application.dto.TeamResponse;
import com.phoenixtask.workspace.application.dto.TeamListQuery;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.phoenixtask.shared.interfaces.PageRequest;
import com.phoenixtask.shared.interfaces.PageResponse;

@RestController
@RequestMapping("/api/workspace/teams")
public class WorkspaceTeamController {

  private final WorkspaceTeamService service;
  private final WorkspaceTeamManagementService managementService;

  public WorkspaceTeamController(
      WorkspaceTeamService service,
      WorkspaceTeamManagementService managementService
  ) {
    this.service = service;
    this.managementService = managementService;
  }

  @GetMapping
  public PageResponse<TeamResponse> listTeams(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      @RequestParam(required = false) String query
  ) {
    PageRequest pageRequest = PageRequest.of(page, pageSize);
    TeamListQuery listQuery = new TeamListQuery(query);
    return service.listTeams(pageRequest, listQuery);
  }

  @PostMapping
  public TeamResponse createTeam(@Valid @RequestBody TeamCreateRequest request) {
    return managementService.createTeam(request);
  }

  @GetMapping("/{teamId}/members")
  public List<MemberResponse> listMembers(@PathVariable Long teamId) {
    return managementService.listMembers(teamId);
  }

  @PostMapping("/{teamId}/members")
  public void addMember(@PathVariable Long teamId, @Valid @RequestBody MemberAddRequest request) {
    managementService.addMember(teamId, request);
  }
}
