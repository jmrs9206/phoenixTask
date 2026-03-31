package com.phoenixtask.workspace.application;

import com.phoenixtask.controlplane.application.audit.AuditDomain;
import com.phoenixtask.controlplane.application.audit.AuditLogService;
import com.phoenixtask.controlplane.application.audit.AuditOutcome;
import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.ForbiddenException;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.shared.security.SecretEncryptionService;
import com.phoenixtask.shared.security.SecretHashingService;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceGitIntegrationRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceGitRepositoryRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceProjectRepository;
import com.phoenixtask.workspace.integrations.GitIntegrationStatus;
import com.phoenixtask.workspace.integrations.GitProvider;
import com.phoenixtask.workspace.integrations.GitRepositoryStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceGitIntegrationService {

  private final WorkspaceGitIntegrationRepository integrationRepository;
  private final WorkspaceGitRepositoryRepository repositoryRepository;
  private final WorkspaceProjectRepository projectRepository;
  private final AuditLogService auditLogService;
  private final SecretHashingService hashingService;
  private final SecretEncryptionService encryptionService;
  private final GitProviderValidationService validationService;
  private final boolean validateOnCreate;
  private final SecureRandom secureRandom = new SecureRandom();

  public WorkspaceGitIntegrationService(
      WorkspaceGitIntegrationRepository integrationRepository,
      WorkspaceGitRepositoryRepository repositoryRepository,
      WorkspaceProjectRepository projectRepository,
      AuditLogService auditLogService,
      SecretHashingService hashingService,
      SecretEncryptionService encryptionService,
      GitProviderValidationService validationService,
      @Value("${phoenixtask.integrations.git.validate-on-create:false}") boolean validateOnCreate
  ) {
    this.integrationRepository = integrationRepository;
    this.repositoryRepository = repositoryRepository;
    this.projectRepository = projectRepository;
    this.auditLogService = auditLogService;
    this.hashingService = hashingService;
    this.encryptionService = encryptionService;
    this.validationService = validationService;
    this.validateOnCreate = validateOnCreate;
  }

  public GitIntegrationResult createIntegration(
      AuthPrincipal principal,
      String providerValue,
      String label,
      String token,
      Boolean validate,
      HttpServletRequest request
  ) {
    GitProvider provider = GitProvider.from(providerValue)
        .orElseThrow(() -> new ValidationException("provider: unsupported"));
    if (label == null || label.isBlank()) {
      throw new ValidationException("label: required");
    }
    if (token == null || token.isBlank()) {
      throw new ValidationException("token: required");
    }
    boolean shouldValidate = Boolean.TRUE.equals(validate) || validateOnCreate;
    if (shouldValidate) {
      try {
        validationService.validateToken(provider, token);
      } catch (ValidationException ex) {
        auditLogService.recordFromRequest(
            AuditDomain.WORKSPACE,
            "GIT_INTEGRATION_VALIDATION_FAILED",
            principal.getTenantCode(),
            principal,
            "GIT_INTEGRATION",
            null,
            AuditOutcome.FAILURE,
            "provider=" + provider.name() + " | " + ex.getMessage(),
            request
        );
        throw ex;
      }
    }
    String tokenHash = hashingService.hashBase64(token);
    String tokenPrefix = token.length() <= 8 ? token : token.substring(0, 8);
    String webhookSecret = generateSecret();
    String storedWebhookSecret = encryptionService.encrypt(webhookSecret);
    GitIntegrationStatus status = shouldValidate
        ? GitIntegrationStatus.CONNECTED
        : GitIntegrationStatus.CONFIGURED;
    WorkspaceGitIntegrationRepository.GitIntegrationRow row = integrationRepository.insert(
        principal.getCompanyId(),
        provider.name(),
        label.trim(),
        tokenHash,
        tokenPrefix,
        storedWebhookSecret,
        status.name()
    );
    auditLogService.recordFromRequest(
        AuditDomain.WORKSPACE,
        status == GitIntegrationStatus.CONNECTED
            ? "GIT_INTEGRATION_CONNECTED"
            : "GIT_INTEGRATION_CONFIGURED",
        principal.getTenantCode(),
        principal,
        "GIT_INTEGRATION",
        row != null ? row.id().toString() : null,
        AuditOutcome.SUCCESS,
        "provider=" + provider.name() + (shouldValidate ? " | validated" : " | validation=skipped"),
        request
    );
    return new GitIntegrationResult(
        Objects.requireNonNull(row),
        webhookSecret,
        buildWebhookPath(provider, principal.getTenantCode(), row.id())
    );
  }

  public List<WorkspaceGitIntegrationRepository.GitIntegrationRow> listIntegrations(AuthPrincipal principal) {
    return integrationRepository.listByCompany(principal.getCompanyId());
  }

  public WorkspaceGitIntegrationRepository.GitIntegrationRow revokeIntegration(
      AuthPrincipal principal,
      Long integrationId,
      HttpServletRequest request
  ) {
    WorkspaceGitIntegrationRepository.GitIntegrationRow row = integrationRepository
        .revoke(integrationId, principal.getCompanyId())
        .orElseThrow(() -> new ResourceNotFoundException("Integration not found"));
    auditLogService.recordFromRequest(
        AuditDomain.WORKSPACE,
        "GIT_INTEGRATION_REVOKED",
        principal.getTenantCode(),
        principal,
        "GIT_INTEGRATION",
        row.id().toString(),
        AuditOutcome.SUCCESS,
        "provider=" + row.provider(),
        request
    );
    return row;
  }

  public WorkspaceGitRepositoryRepository.GitRepositoryRow linkRepository(
      AuthPrincipal principal,
      Long integrationId,
      Long projectId,
      String repoOwner,
      String repoName,
      String defaultBranch,
      HttpServletRequest request
  ) {
    WorkspaceGitIntegrationRepository.GitIntegrationRow integration = integrationRepository.findById(integrationId)
        .orElseThrow(() -> new ResourceNotFoundException("Integration not found"));
    if (!integration.companyId().equals(principal.getCompanyId())) {
      throw new ForbiddenException("Integration not available for tenant");
    }
    WorkspaceProjectRepository.ProjectRow project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    if (!project.companyId().equals(principal.getCompanyId())) {
      throw new ForbiddenException("Project not available for tenant");
    }
    if (repoOwner == null || repoOwner.isBlank()) {
      throw new ValidationException("repoOwner: required");
    }
    if (repoName == null || repoName.isBlank()) {
      throw new ValidationException("repoName: required");
    }
    if (repositoryRepository.exists(integrationId, repoOwner.trim(), repoName.trim())) {
      throw new ValidationException("Repository already linked");
    }
    WorkspaceGitRepositoryRepository.GitRepositoryRow row = repositoryRepository.insert(
        integrationId,
        projectId,
        repoOwner.trim(),
        repoName.trim(),
        defaultBranch != null ? defaultBranch.trim() : null,
        GitRepositoryStatus.CONNECTED.name()
    );
    auditLogService.recordFromRequest(
        AuditDomain.WORKSPACE,
        "GIT_REPOSITORY_LINKED",
        principal.getTenantCode(),
        principal,
        "GIT_REPOSITORY",
        row.id().toString(),
        AuditOutcome.SUCCESS,
        repoOwner + "/" + repoName,
        request
    );
    return row;
  }

  public List<WorkspaceGitRepositoryRepository.GitRepositoryRow> listRepositories(
      AuthPrincipal principal,
      Long integrationId
  ) {
    WorkspaceGitIntegrationRepository.GitIntegrationRow integration = integrationRepository.findById(integrationId)
        .orElseThrow(() -> new ResourceNotFoundException("Integration not found"));
    if (!integration.companyId().equals(principal.getCompanyId())) {
      throw new ForbiddenException("Integration not available for tenant");
    }
    return repositoryRepository.listByIntegration(integrationId);
  }

  public WorkspaceGitIntegrationRepository.GitIntegrationRow findIntegration(Long integrationId) {
    return integrationRepository.findById(integrationId)
        .orElseThrow(() -> new ResourceNotFoundException("Integration not found"));
  }

  private String generateSecret() {
    byte[] buffer = new byte[32];
    secureRandom.nextBytes(buffer);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
  }

  private String buildWebhookPath(GitProvider provider, String tenantCode, Long integrationId) {
    return "/api/webhooks/git/" + tenantCode + "/" + provider.name().toLowerCase() + "/" + integrationId;
  }

  public record GitIntegrationResult(
      WorkspaceGitIntegrationRepository.GitIntegrationRow integration,
      String webhookSecret,
      String webhookPath
  ) {}
}
