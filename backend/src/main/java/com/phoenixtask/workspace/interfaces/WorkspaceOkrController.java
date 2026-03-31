package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.workspace.application.WorkspaceOkrService;
import com.phoenixtask.workspace.application.dto.OkrCheckinCreateRequest;
import com.phoenixtask.workspace.application.dto.OkrCheckinResponse;
import com.phoenixtask.workspace.application.dto.OkrKeyResultCreateRequest;
import com.phoenixtask.workspace.application.dto.OkrKeyResultResponse;
import com.phoenixtask.workspace.application.dto.OkrInitiativeCreateRequest;
import com.phoenixtask.workspace.application.dto.OkrInitiativeResponse;
import com.phoenixtask.workspace.application.dto.OkrObjectiveCloseRequest;
import com.phoenixtask.workspace.application.dto.OkrObjectiveCreateRequest;
import com.phoenixtask.workspace.application.dto.OkrObjectiveDetailResponse;
import com.phoenixtask.workspace.application.dto.OkrObjectiveResponse;
import com.phoenixtask.security.AuthPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace/okr")
public class WorkspaceOkrController {

  private final WorkspaceOkrService okrService;

  public WorkspaceOkrController(WorkspaceOkrService okrService) {
    this.okrService = okrService;
  }

  @GetMapping("/projects/{projectId}/objectives")
  public List<OkrObjectiveResponse> listObjectives(@PathVariable Long projectId) {
    return okrService.listObjectives(projectId);
  }

  @GetMapping("/projects/{projectId}/objectives/details")
  public List<OkrObjectiveDetailResponse> listObjectiveDetails(@PathVariable Long projectId) {
    return okrService.listObjectiveDetails(projectId);
  }

  @PostMapping("/projects/{projectId}/objectives")
  public OkrObjectiveResponse createObjective(
      @PathVariable Long projectId,
      @Valid @RequestBody OkrObjectiveCreateRequest request
  ) {
    return okrService.createObjective(projectId, request);
  }

  @GetMapping("/objectives/{objectiveId}")
  public OkrObjectiveDetailResponse getObjective(@PathVariable Long objectiveId) {
    return okrService.getObjective(objectiveId);
  }

  @PostMapping("/objectives/{objectiveId}/key-results")
  public OkrKeyResultResponse addKeyResult(
      @PathVariable Long objectiveId,
      @Valid @RequestBody OkrKeyResultCreateRequest request
  ) {
    return okrService.addKeyResult(objectiveId, request);
  }

  @PostMapping("/objectives/{objectiveId}/check-ins")
  public OkrCheckinResponse addCheckin(
      @PathVariable Long objectiveId,
      @Valid @RequestBody OkrCheckinCreateRequest request
  ) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    AuthPrincipal principal = authentication != null && authentication.getPrincipal() instanceof AuthPrincipal
        ? (AuthPrincipal) authentication.getPrincipal()
        : null;
    Long userId = principal != null ? principal.getUserId() : null;
    if (userId == null) {
      throw new IllegalStateException("Missing authenticated user");
    }
    return okrService.addCheckin(objectiveId, userId, request);
  }

  @PostMapping("/objectives/{objectiveId}/close")
  public OkrObjectiveResponse closeObjective(
      @PathVariable Long objectiveId,
      @Valid @RequestBody OkrObjectiveCloseRequest request
  ) {
    return okrService.closeObjective(objectiveId, request);
  }

  @PostMapping("/objectives/{objectiveId}/initiatives")
  public OkrInitiativeResponse addInitiative(
      @PathVariable Long objectiveId,
      @Valid @RequestBody OkrInitiativeCreateRequest request
  ) {
    return okrService.addInitiative(objectiveId, request);
  }
}
