package com.phoenixtask.workspace.application;

import com.phoenixtask.workspace.application.dto.IssueCodeLinkResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceGitRepositoryRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceIssueCodeLinkRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceIssueRepository;
import com.phoenixtask.workspace.integrations.GitProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceIssueCodeLinkService {

  private final WorkspaceIssueCodeLinkRepository linkRepository;
  private final WorkspaceIssueRepository issueRepository;
  private final WorkspaceGitRepositoryRepository gitRepositoryRepository;

  public WorkspaceIssueCodeLinkService(
      WorkspaceIssueCodeLinkRepository linkRepository,
      WorkspaceIssueRepository issueRepository,
      WorkspaceGitRepositoryRepository gitRepositoryRepository
  ) {
    this.linkRepository = linkRepository;
    this.issueRepository = issueRepository;
    this.gitRepositoryRepository = gitRepositoryRepository;
  }

  public List<IssueCodeLinkResponse> listByIssue(Long issueId) {
    return linkRepository.findByIssue(issueId).stream()
        .map(row -> new IssueCodeLinkResponse(
            row.id(),
            row.issueId(),
            row.provider(),
            row.artifactType(),
            row.externalId(),
            row.title(),
            row.url(),
            row.authorName(),
            row.externalCreatedAt(),
            row.createdAt(),
            row.repoOwner(),
            row.repoName()
        ))
        .toList();
  }

  public void recordLinks(
      Long integrationId,
      GitProvider provider,
      String repoFullName,
      List<IssueCodeLinkCandidate> candidates
  ) {
    if (repoFullName == null || repoFullName.isBlank() || candidates == null || candidates.isEmpty()) {
      return;
    }
    RepoParts repoParts = splitRepoFullName(repoFullName);
    if (repoParts == null) {
      return;
    }
    Optional<WorkspaceGitRepositoryRepository.GitRepositoryRow> repo = gitRepositoryRepository
        .findByIntegrationAndOwnerAndName(integrationId, repoParts.owner(), repoParts.name());
    if (repo.isEmpty()) {
      return;
    }
    WorkspaceGitRepositoryRepository.GitRepositoryRow repoRow = repo.get();
    List<IssueCodeLinkCandidate> unique = new ArrayList<>(candidates);
    for (IssueCodeLinkCandidate candidate : unique) {
      if (candidate.issueKey() == null || candidate.issueKey().isBlank()) {
        continue;
      }
      Optional<WorkspaceIssueRepository.IssueLookupRow> issue =
          issueRepository.findByIssueKey(candidate.issueKey().toUpperCase(Locale.ROOT));
      if (issue.isEmpty()) {
        continue;
      }
      WorkspaceIssueRepository.IssueLookupRow issueRow = issue.get();
      if (!issueRow.projectId().equals(repoRow.projectId())) {
        continue;
      }
      linkRepository.insertIfAbsent(
          issueRow.id(),
          issueRow.projectId(),
          integrationId,
          repoRow.id(),
          provider.name(),
          candidate.artifactType().name(),
          candidate.externalId(),
          candidate.title(),
          candidate.url(),
          candidate.authorName(),
          candidate.externalCreatedAt()
      );
    }
  }

  private RepoParts splitRepoFullName(String repoFullName) {
    String trimmed = repoFullName.trim();
    int slash = trimmed.lastIndexOf('/');
    if (slash <= 0 || slash == trimmed.length() - 1) {
      return null;
    }
    String owner = trimmed.substring(0, slash);
    String name = trimmed.substring(slash + 1);
    if (owner.isBlank() || name.isBlank()) {
      return null;
    }
    return new RepoParts(owner, name);
  }

  private record RepoParts(String owner, String name) {}
}
