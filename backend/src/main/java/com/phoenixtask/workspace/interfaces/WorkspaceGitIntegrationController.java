package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.UnauthorizedException;
import com.phoenixtask.workspace.application.WorkspaceGitIntegrationService;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceGitIntegrationRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceGitRepositoryRepository;
import com.phoenixtask.workspace.interfaces.dto.GitIntegrationCreateRequest;
import com.phoenixtask.workspace.interfaces.dto.GitIntegrationCreateResponse;
import com.phoenixtask.workspace.interfaces.dto.GitIntegrationResponse;
import com.phoenixtask.workspace.interfaces.dto.GitRepositoryLinkRequest;
import com.phoenixtask.workspace.interfaces.dto.GitRepositoryResponse;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/workspace/integrations/git")
public class WorkspaceGitIntegrationController {

  private final WorkspaceGitIntegrationService integrationService;

  public WorkspaceGitIntegrationController(WorkspaceGitIntegrationService integrationService) {
    this.integrationService = integrationService;
  }

  @PostMapping
  public GitIntegrationCreateResponse createIntegration(
      @Valid @RequestBody GitIntegrationCreateRequest request,
      HttpServletRequest httpRequest
  ) {
    AuthPrincipal principal = requirePrincipal();
    WorkspaceGitIntegrationService.GitIntegrationResult result = integrationService.createIntegration(
        principal,
        request.provider(),
        request.label(),
        request.token(),
        request.validate(),
        httpRequest
    );
    WorkspaceGitIntegrationRepository.GitIntegrationRow row = result.integration();
    return new GitIntegrationCreateResponse(
        row.id(),
        row.provider(),
        row.label(),
        row.status(),
        row.tokenPrefix(),
        result.webhookSecret(),
        result.webhookPath(),
        row.createdAt()
    );
  }

  @GetMapping
  public List<GitIntegrationResponse> listIntegrations() {
    AuthPrincipal principal = requirePrincipal();
    return integrationService.listIntegrations(principal).stream()
        .map(row -> new GitIntegrationResponse(
            row.id(),
            row.provider(),
            row.label(),
            row.status(),
            row.tokenPrefix(),
            row.createdAt(),
            row.revokedAt()
        ))
        .toList();
  }

  @PostMapping("/{integrationId}/revoke")
  public GitIntegrationResponse revokeIntegration(
      @PathVariable Long integrationId,
      HttpServletRequest httpRequest
  ) {
    AuthPrincipal principal = requirePrincipal();
    WorkspaceGitIntegrationRepository.GitIntegrationRow row =
        integrationService.revokeIntegration(principal, integrationId, httpRequest);
    return new GitIntegrationResponse(
        row.id(),
        row.provider(),
        row.label(),
        row.status(),
        row.tokenPrefix(),
        row.createdAt(),
        row.revokedAt()
    );
  }

  @PostMapping("/{integrationId}/projects/{projectId}/repos")
  public GitRepositoryResponse linkRepository(
      @PathVariable Long integrationId,
      @PathVariable Long projectId,
      @Valid @RequestBody GitRepositoryLinkRequest request,
      HttpServletRequest httpRequest
  ) {
    AuthPrincipal principal = requirePrincipal();
    WorkspaceGitRepositoryRepository.GitRepositoryRow row = integrationService.linkRepository(
        principal,
        integrationId,
        projectId,
        request.repoOwner(),
        request.repoName(),
        request.defaultBranch(),
        httpRequest
    );
    String fullName = row.repoOwner() + "/" + row.repoName();
    WorkspaceGitIntegrationRepository.GitIntegrationRow integration =
        integrationService.findIntegration(integrationId);
    return new GitRepositoryResponse(
        row.id(),
        row.integrationId(),
        row.projectId(),
        integration.provider(),
        row.repoOwner(),
        row.repoName(),
        fullName,
        row.defaultBranch(),
        row.status(),
        row.createdAt()
    );
  }

  @GetMapping("/{integrationId}/repos")
  public List<GitRepositoryResponse> listRepositories(@PathVariable Long integrationId) {
    AuthPrincipal principal = requirePrincipal();
    WorkspaceGitIntegrationRepository.GitIntegrationRow integration =
        integrationService.findIntegration(integrationId);
    return integrationService.listRepositories(principal, integrationId).stream()
        .map(row -> new GitRepositoryResponse(
            row.id(),
            row.integrationId(),
            row.projectId(),
            integration.provider(),
            row.repoOwner(),
            row.repoName(),
            row.repoOwner() + "/" + row.repoName(),
            row.defaultBranch(),
            row.status(),
            row.createdAt()
        ))
        .toList();
  }

  private AuthPrincipal requirePrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
      throw new UnauthorizedException("Missing Authorization header");
    }
    return principal;
  }
}
