package com.phoenixtask.publicapi.domain;

import java.util.Arrays;
import java.util.Optional;

public enum PublicApiScope {
  PROJECTS_READ("projects.read"),
  ISSUES_READ("issues.read");

  private final String code;

  PublicApiScope(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static Optional<PublicApiScope> fromCode(String code) {
    if (code == null || code.isBlank()) {
      return Optional.empty();
    }
    return Arrays.stream(values())
        .filter(scope -> scope.code.equals(code))
        .findFirst();
  }
}
