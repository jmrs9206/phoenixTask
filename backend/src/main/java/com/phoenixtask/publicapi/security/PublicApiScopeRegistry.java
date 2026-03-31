package com.phoenixtask.publicapi.security;

import com.phoenixtask.publicapi.domain.PublicApiScope;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
public class PublicApiScopeRegistry {

  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  private final List<PublicApiScopeRule> rules = List.of(
      new PublicApiScopeRule(HttpMethod.GET, "/api/public/projects", PublicApiScope.PROJECTS_READ),
      new PublicApiScopeRule(HttpMethod.GET, "/api/public/projects/*", PublicApiScope.PROJECTS_READ),
      new PublicApiScopeRule(HttpMethod.GET, "/api/public/issues", PublicApiScope.ISSUES_READ),
      new PublicApiScopeRule(HttpMethod.GET, "/api/public/issues/*", PublicApiScope.ISSUES_READ)
  );

  public Optional<PublicApiScopeRule> match(String method, String path) {
    for (PublicApiScopeRule rule : rules) {
      if (rule.method().matches(method) && PATH_MATCHER.match(rule.pathPattern(), path)) {
        return Optional.of(rule);
      }
    }
    return Optional.empty();
  }
}
