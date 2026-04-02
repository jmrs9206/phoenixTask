package com.phoenixtask.workspace.application;

import java.util.List;
import java.util.Set;

public final class WorkspaceIssueTaxonomy {
  // TODO(Phase Y): move status/category/priority to configurable tables with admin permissions.

  public static final List<String> STATUS_ORDER = List.of(
      "BACKLOG",
      "READY",
      "IN_PROGRESS",
      "IN_REVIEW",
      "BLOCKED",
      "DISCARDED",
      "DONE"
  );

  public static final Set<String> STATUSES = Set.copyOf(STATUS_ORDER);

  public static final Set<String> PRIORITIES = Set.of("EPIC", "HIGH", "MEDIUM", "LOW");

  public static final Set<String> CATEGORIES = Set.of(
      "FEATURE",
      "TASK",
      "IMPROVEMENT",
      "BUG",
      "HOTFIX",
      "TECHNICAL_DEBT"
  );

  private WorkspaceIssueTaxonomy() {}
}
