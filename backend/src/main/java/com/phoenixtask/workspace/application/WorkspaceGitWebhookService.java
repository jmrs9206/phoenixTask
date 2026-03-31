package com.phoenixtask.workspace.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phoenixtask.controlplane.application.TenantRegistryLookupService;
import com.phoenixtask.controlplane.application.audit.AuditDomain;
import com.phoenixtask.controlplane.application.audit.AuditLogService;
import com.phoenixtask.controlplane.application.audit.AuditOutcome;
import com.phoenixtask.shared.error.ForbiddenException;
import com.phoenixtask.shared.error.TenantInactiveException;
import com.phoenixtask.shared.error.TenantNotFoundException;
import com.phoenixtask.shared.error.UnauthorizedException;
import com.phoenixtask.shared.tenant.TenantLifecyclePolicy;
import com.phoenixtask.shared.tenant.TenantMetadata;
import com.phoenixtask.shared.security.SecretEncryptionService;
import com.phoenixtask.workspace.application.IssueCodeArtifactType;
import com.phoenixtask.workspace.application.IssueCodeLinkCandidate;
import com.phoenixtask.workspace.application.WorkspaceIssueCodeLinkService;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceGitIntegrationRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceGitWebhookEventRepository;
import com.phoenixtask.workspace.infrastructure.tenant.TenantContext;
import com.phoenixtask.workspace.integrations.GitProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceGitWebhookService {

  private static final String GITHUB_SIGNATURE_HEADER = "X-Hub-Signature-256";
  private static final String GITHUB_EVENT_HEADER = "X-GitHub-Event";
  private static final String GITHUB_DELIVERY_HEADER = "X-GitHub-Delivery";
  private static final String GITLAB_EVENT_HEADER = "X-Gitlab-Event";
  private static final String GITLAB_DELIVERY_HEADER = "X-Gitlab-Event-UUID";
  private static final String GITLAB_TOKEN_HEADER = "X-Gitlab-Token";

  private final WorkspaceGitIntegrationRepository integrationRepository;
  private final WorkspaceGitWebhookEventRepository webhookEventRepository;
  private final WorkspaceIssueCodeLinkService codeLinkService;
  private final TenantRegistryLookupService tenantRegistryLookupService;
  private final AuditLogService auditLogService;
  private final ObjectMapper objectMapper;
  private final SecretEncryptionService encryptionService;

  private static final Pattern ISSUE_KEY_PATTERN = Pattern.compile("([A-Z][A-Z0-9]+-\\d+)");

  public WorkspaceGitWebhookService(
      WorkspaceGitIntegrationRepository integrationRepository,
      WorkspaceGitWebhookEventRepository webhookEventRepository,
      WorkspaceIssueCodeLinkService codeLinkService,
      TenantRegistryLookupService tenantRegistryLookupService,
      AuditLogService auditLogService,
      ObjectMapper objectMapper,
      SecretEncryptionService encryptionService
  ) {
    this.integrationRepository = integrationRepository;
    this.webhookEventRepository = webhookEventRepository;
    this.codeLinkService = codeLinkService;
    this.tenantRegistryLookupService = tenantRegistryLookupService;
    this.auditLogService = auditLogService;
    this.objectMapper = objectMapper;
    this.encryptionService = encryptionService;
  }

  public void handleWebhook(
      String tenantCode,
      GitProvider provider,
      Long integrationId,
      byte[] payload,
      HttpServletRequest request
  ) {
    TenantMetadata metadata = tenantRegistryLookupService.findMetadataByCode(tenantCode)
        .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));
    TenantLifecyclePolicy.requireActive(metadata.getStatus());
    TenantContext.set(metadata);
    try {
      WorkspaceGitIntegrationRepository.GitIntegrationRow integration = integrationRepository.findById(integrationId)
          .orElseThrow(() -> new ForbiddenException("Integration not found"));
      if (!provider.name().equalsIgnoreCase(integration.provider())) {
        throw new ForbiddenException("Provider mismatch");
      }
      String secret = encryptionService.decryptIfNeeded(integration.webhookSecret());
      validateSignature(provider, secret, payload, request);
      JsonNode root = parsePayload(payload);
      WebhookInfo info = extractInfo(provider, root, request);
      webhookEventRepository.insert(
          integrationId,
          provider.name(),
          info.eventType(),
          info.deliveryId(),
          info.repoFullName(),
          true
      );
      auditLogService.recordFromRequest(
          AuditDomain.WORKSPACE,
          "GIT_WEBHOOK_RECEIVED",
          tenantCode,
          null,
          "GIT_WEBHOOK",
          info.repoFullName(),
          AuditOutcome.SUCCESS,
          provider.name() + ":" + info.eventType(),
          request
      );
      if (root != null) {
        List<IssueCodeLinkCandidate> candidates = extractTraceabilityCandidates(provider, info.eventType(), root);
        codeLinkService.recordLinks(integrationId, provider, info.repoFullName(), candidates);
      }
    } finally {
      TenantContext.clear();
    }
  }

  private void validateSignature(
      GitProvider provider,
      String secret,
      byte[] payload,
      HttpServletRequest request
  ) {
    if (provider == GitProvider.GITHUB) {
      String signature = request.getHeader(GITHUB_SIGNATURE_HEADER);
      if (signature == null || signature.isBlank()) {
        throw new UnauthorizedException("Missing webhook signature");
      }
      String expected = "sha256=" + hmacSha256Hex(secret, payload);
      if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
        throw new UnauthorizedException("Invalid webhook signature");
      }
      return;
    }
    if (provider == GitProvider.GITLAB) {
      String token = request.getHeader(GITLAB_TOKEN_HEADER);
      if (token == null || token.isBlank()) {
        throw new UnauthorizedException("Missing webhook token");
      }
      if (!MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), secret.getBytes(StandardCharsets.UTF_8))) {
        throw new UnauthorizedException("Invalid webhook token");
      }
      return;
    }
    throw new UnauthorizedException("Unsupported provider");
  }

  private WebhookInfo extractInfo(
      GitProvider provider,
      JsonNode root,
      HttpServletRequest request
  ) {
    String eventType = provider == GitProvider.GITHUB
        ? Optional.ofNullable(request.getHeader(GITHUB_EVENT_HEADER)).orElse("unknown")
        : Optional.ofNullable(request.getHeader(GITLAB_EVENT_HEADER)).orElse("unknown");
    String deliveryId = provider == GitProvider.GITHUB
        ? request.getHeader(GITHUB_DELIVERY_HEADER)
        : request.getHeader(GITLAB_DELIVERY_HEADER);
    return new WebhookInfo(eventType, deliveryId, extractRepoFullName(root));
  }

  private JsonNode parsePayload(byte[] payload) {
    try {
      return objectMapper.readTree(payload);
    } catch (Exception ex) {
      return null;
    }
  }

  private String extractRepoFullName(JsonNode root) {
    if (root == null) {
      return null;
    }
    JsonNode repoNode = root.get("repository");
    if (repoNode == null) {
      return null;
    }
    if (repoNode.has("full_name")) {
      return repoNode.get("full_name").asText();
    }
    if (repoNode.has("path_with_namespace")) {
      return repoNode.get("path_with_namespace").asText();
    }
    if (repoNode.has("name")) {
      return repoNode.get("name").asText();
    }
    return null;
  }

  private String extractRepoWebUrl(JsonNode root) {
    if (root == null) {
      return null;
    }
    JsonNode repoNode = root.get("repository");
    if (repoNode == null) {
      return null;
    }
    if (repoNode.has("html_url")) {
      return repoNode.get("html_url").asText();
    }
    if (repoNode.has("web_url")) {
      return repoNode.get("web_url").asText();
    }
    return null;
  }

  private List<IssueCodeLinkCandidate> extractTraceabilityCandidates(
      GitProvider provider,
      String eventType,
      JsonNode root
  ) {
    if (root == null || eventType == null) {
      return List.of();
    }
    String normalized = eventType.toLowerCase(Locale.ROOT);
    String repoWebUrl = extractRepoWebUrl(root);
    List<IssueCodeLinkCandidate> candidates = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    if (provider == GitProvider.GITHUB) {
      if ("push".equals(normalized)) {
        handleGithubPush(root, repoWebUrl, candidates, seen);
      } else if ("pull_request".equals(normalized)) {
        handleGithubPullRequest(root, repoWebUrl, candidates, seen);
      }
    } else if (provider == GitProvider.GITLAB) {
      if (normalized.contains("push")) {
        handleGitlabPush(root, repoWebUrl, candidates, seen);
      } else if (normalized.contains("merge request")) {
        handleGitlabMergeRequest(root, repoWebUrl, candidates, seen);
      }
    }
    return candidates;
  }

  private void handleGithubPush(
      JsonNode root,
      String repoWebUrl,
      List<IssueCodeLinkCandidate> candidates,
      Set<String> seen
  ) {
    String ref = root.path("ref").asText(null);
    String branch = normalizeBranch(ref);
    Optional<String> branchIssue = extractIssueKey(branch);
    branchIssue.ifPresent(issueKey -> addCandidate(
        candidates,
        seen,
        new IssueCodeLinkCandidate(
            issueKey,
            IssueCodeArtifactType.BRANCH,
            branch,
            "Branch " + branch,
            buildBranchUrl(repoWebUrl, branch, GitProvider.GITHUB),
            null,
            null
        )
    ));
    JsonNode commits = root.path("commits");
    if (commits.isArray()) {
      for (JsonNode commit : commits) {
        String message = commit.path("message").asText("");
        Optional<String> issueKey = extractIssueKey(message);
        if (issueKey.isEmpty() && branchIssue.isPresent()) {
          issueKey = branchIssue;
        }
        if (issueKey.isEmpty()) {
          continue;
        }
        String sha = commit.path("id").asText(null);
        if (sha == null) {
          sha = commit.path("sha").asText(null);
        }
        if (sha == null || sha.isBlank()) {
          continue;
        }
        String title = firstLine(message);
        String url = commit.path("url").asText(null);
        if (url == null || url.isBlank()) {
          url = buildCommitUrl(repoWebUrl, sha, GitProvider.GITHUB);
        }
        String authorName = commit.path("author").path("name").asText(null);
        Instant timestamp = parseInstant(commit.path("timestamp").asText(null));
        addCandidate(candidates, seen, new IssueCodeLinkCandidate(
            issueKey.get(),
            IssueCodeArtifactType.COMMIT,
            sha,
            title,
            url,
            authorName,
            timestamp
        ));
      }
    }
  }

  private void handleGithubPullRequest(
      JsonNode root,
      String repoWebUrl,
      List<IssueCodeLinkCandidate> candidates,
      Set<String> seen
  ) {
    JsonNode pr = root.path("pull_request");
    if (pr.isMissingNode()) {
      return;
    }
    String title = pr.path("title").asText("");
    String headRef = pr.path("head").path("ref").asText("");
    Optional<String> issueKey = extractIssueKey(title);
    Optional<String> branchIssue = extractIssueKey(headRef);
    if (issueKey.isEmpty()) {
      issueKey = branchIssue;
    }
    if (issueKey.isEmpty()) {
      return;
    }
    String number = pr.path("number").asText(null);
    if (number == null || number.isBlank()) {
      number = pr.path("id").asText(null);
    }
    if (number == null || number.isBlank()) {
      return;
    }
    String url = pr.path("html_url").asText(null);
    String author = pr.path("user").path("login").asText(null);
    Instant createdAt = parseInstant(pr.path("created_at").asText(null));
    addCandidate(candidates, seen, new IssueCodeLinkCandidate(
        issueKey.get(),
        IssueCodeArtifactType.PULL_REQUEST,
        number,
        title,
        url,
        author,
        createdAt
    ));
    if (branchIssue.isPresent()) {
      addCandidate(candidates, seen, new IssueCodeLinkCandidate(
          branchIssue.get(),
          IssueCodeArtifactType.BRANCH,
          headRef,
          "Branch " + headRef,
          buildBranchUrl(repoWebUrl, headRef, GitProvider.GITHUB),
          null,
          null
      ));
    }
  }

  private void handleGitlabPush(
      JsonNode root,
      String repoWebUrl,
      List<IssueCodeLinkCandidate> candidates,
      Set<String> seen
  ) {
    String ref = root.path("ref").asText(null);
    String branch = normalizeBranch(ref);
    Optional<String> branchIssue = extractIssueKey(branch);
    branchIssue.ifPresent(issueKey -> addCandidate(
        candidates,
        seen,
        new IssueCodeLinkCandidate(
            issueKey,
            IssueCodeArtifactType.BRANCH,
            branch,
            "Branch " + branch,
            buildBranchUrl(repoWebUrl, branch, GitProvider.GITLAB),
            null,
            null
        )
    ));
    JsonNode commits = root.path("commits");
    if (commits.isArray()) {
      for (JsonNode commit : commits) {
        String message = commit.path("message").asText("");
        Optional<String> issueKey = extractIssueKey(message);
        if (issueKey.isEmpty() && branchIssue.isPresent()) {
          issueKey = branchIssue;
        }
        if (issueKey.isEmpty()) {
          continue;
        }
        String sha = commit.path("id").asText(null);
        if (sha == null || sha.isBlank()) {
          continue;
        }
        String title = firstLine(message);
        String url = commit.path("url").asText(null);
        if (url == null || url.isBlank()) {
          url = buildCommitUrl(repoWebUrl, sha, GitProvider.GITLAB);
        }
        String authorName = commit.path("author").path("name").asText(null);
        Instant timestamp = parseInstant(commit.path("timestamp").asText(null));
        addCandidate(candidates, seen, new IssueCodeLinkCandidate(
            issueKey.get(),
            IssueCodeArtifactType.COMMIT,
            sha,
            title,
            url,
            authorName,
            timestamp
        ));
      }
    }
  }

  private void handleGitlabMergeRequest(
      JsonNode root,
      String repoWebUrl,
      List<IssueCodeLinkCandidate> candidates,
      Set<String> seen
  ) {
    JsonNode attributes = root.path("object_attributes");
    if (attributes.isMissingNode()) {
      return;
    }
    String title = attributes.path("title").asText("");
    String sourceBranch = attributes.path("source_branch").asText("");
    Optional<String> issueKey = extractIssueKey(title);
    Optional<String> branchIssue = extractIssueKey(sourceBranch);
    if (issueKey.isEmpty()) {
      issueKey = branchIssue;
    }
    if (issueKey.isEmpty()) {
      return;
    }
    String iid = attributes.path("iid").asText(null);
    if (iid == null || iid.isBlank()) {
      iid = attributes.path("id").asText(null);
    }
    if (iid == null || iid.isBlank()) {
      return;
    }
    String url = attributes.path("url").asText(null);
    String author = root.path("user").path("name").asText(null);
    Instant createdAt = parseInstant(attributes.path("created_at").asText(null));
    addCandidate(candidates, seen, new IssueCodeLinkCandidate(
        issueKey.get(),
        IssueCodeArtifactType.MERGE_REQUEST,
        iid,
        title,
        url,
        author,
        createdAt
    ));
    if (branchIssue.isPresent()) {
      addCandidate(candidates, seen, new IssueCodeLinkCandidate(
          branchIssue.get(),
          IssueCodeArtifactType.BRANCH,
          sourceBranch,
          "Branch " + sourceBranch,
          buildBranchUrl(repoWebUrl, sourceBranch, GitProvider.GITLAB),
          null,
          null
      ));
    }
  }

  private String normalizeBranch(String ref) {
    if (ref == null) {
      return null;
    }
    if (ref.startsWith("refs/heads/")) {
      return ref.substring("refs/heads/".length());
    }
    return ref;
  }

  private Optional<String> extractIssueKey(String text) {
    if (text == null || text.isBlank()) {
      return Optional.empty();
    }
    Matcher matcher = ISSUE_KEY_PATTERN.matcher(text.toUpperCase(Locale.ROOT));
    if (matcher.find()) {
      return Optional.ofNullable(matcher.group(1));
    }
    return Optional.empty();
  }

  private String firstLine(String message) {
    if (message == null) {
      return null;
    }
    int idx = message.indexOf('\n');
    String first = idx >= 0 ? message.substring(0, idx) : message;
    return trimToLength(first, 200);
  }

  private String buildBranchUrl(String repoWebUrl, String branch, GitProvider provider) {
    if (repoWebUrl == null || repoWebUrl.isBlank() || branch == null || branch.isBlank()) {
      return null;
    }
    if (provider == GitProvider.GITLAB) {
      return repoWebUrl + "/-/tree/" + branch;
    }
    return repoWebUrl + "/tree/" + branch;
  }

  private String buildCommitUrl(String repoWebUrl, String sha, GitProvider provider) {
    if (repoWebUrl == null || repoWebUrl.isBlank() || sha == null || sha.isBlank()) {
      return null;
    }
    if (provider == GitProvider.GITLAB) {
      return repoWebUrl + "/-/commit/" + sha;
    }
    return repoWebUrl + "/commit/" + sha;
  }

  private Instant parseInstant(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Instant.parse(value);
    } catch (Exception ex) {
      return null;
    }
  }

  private void addCandidate(
      List<IssueCodeLinkCandidate> candidates,
      Set<String> seen,
      IssueCodeLinkCandidate candidate
  ) {
    if (candidate == null || candidate.externalId() == null || candidate.externalId().isBlank()) {
      return;
    }
    String key = candidate.issueKey() + ":" + candidate.artifactType().name() + ":" + candidate.externalId();
    if (seen.add(key)) {
      candidates.add(new IssueCodeLinkCandidate(
          candidate.issueKey(),
          candidate.artifactType(),
          candidate.externalId(),
          trimToLength(candidate.title(), 300),
          candidate.url(),
          trimToLength(candidate.authorName(), 120),
          candidate.externalCreatedAt()
      ));
    }
  }

  private String trimToLength(String value, int maxLength) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.length() <= maxLength) {
      return trimmed;
    }
    return trimmed.substring(0, maxLength);
  }

  private String hmacSha256Hex(String secret, byte[] payload) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] digest = mac.doFinal(payload);
      return HexFormat.of().formatHex(digest);
    } catch (Exception ex) {
      throw new UnauthorizedException("Unable to verify webhook signature");
    }
  }

  private record WebhookInfo(String eventType, String deliveryId, String repoFullName) {}
}
