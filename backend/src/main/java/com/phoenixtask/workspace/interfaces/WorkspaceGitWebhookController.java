package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.WorkspaceGitWebhookService;
import com.phoenixtask.workspace.integrations.GitProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/git")
public class WorkspaceGitWebhookController {

  private final WorkspaceGitWebhookService webhookService;

  public WorkspaceGitWebhookController(WorkspaceGitWebhookService webhookService) {
    this.webhookService = webhookService;
  }

  @PostMapping("/{tenantCode}/{provider}/{integrationId}")
  public ResponseEntity<Void> receive(
      @PathVariable String tenantCode,
      @PathVariable String provider,
      @PathVariable Long integrationId,
      HttpServletRequest request
  ) throws IOException {
    GitProvider gitProvider = GitProvider.from(provider)
        .orElseThrow(() -> new ValidationException("provider: unsupported"));
    byte[] payload = request.getInputStream().readAllBytes();
    webhookService.handleWebhook(tenantCode, gitProvider, integrationId, payload, request);
    return ResponseEntity.ok().build();
  }
}
