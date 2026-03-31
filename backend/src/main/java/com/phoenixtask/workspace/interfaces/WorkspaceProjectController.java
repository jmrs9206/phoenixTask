package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.workspace.application.WorkspaceProjectManagementService;
import com.phoenixtask.workspace.application.WorkspaceProjectService;
import com.phoenixtask.workspace.application.dto.MemberAddRequest;
import com.phoenixtask.workspace.application.dto.MemberResponse;
import com.phoenixtask.workspace.application.dto.ProjectCreateRequest;
import com.phoenixtask.workspace.application.dto.ProjectResponse;
import com.phoenixtask.workspace.application.dto.ProjectListQuery;
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
@RequestMapping("/api/workspace/projects")
public class WorkspaceProjectController {

  private final WorkspaceProjectService service;
  private final WorkspaceProjectManagementService managementService;

  public WorkspaceProjectController(
      WorkspaceProjectService service,
      WorkspaceProjectManagementService managementService
  ) {
    this.service = service;
    this.managementService = managementService;
  }

  @GetMapping
  public PageResponse<ProjectResponse> listProjects(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String query
  ) {
    PageRequest pageRequest = PageRequest.of(page, pageSize);
    ProjectListQuery listQuery = new ProjectListQuery(status, query);
    return service.listProjects(pageRequest, listQuery);
  }

  @PostMapping
  public ProjectResponse createProject(@Valid @RequestBody ProjectCreateRequest request) {
    return managementService.createProject(request);
  }



  @GetMapping("/{projectId}/members")
  public List<MemberResponse> listMembers(@PathVariable Long projectId) {
    return managementService.listMembers(projectId);
  }

  @PostMapping("/{projectId}/members")
  public void addMember(@PathVariable Long projectId, @Valid @RequestBody MemberAddRequest request) {
    managementService.addMember(projectId, request);
  }
}
