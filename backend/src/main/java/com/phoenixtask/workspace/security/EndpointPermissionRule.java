package com.phoenixtask.workspace.security;

import org.springframework.http.HttpMethod;

public record EndpointPermissionRule(
    HttpMethod method,
    String pathPattern,
    PermissionCode permission,
    ContextResolverType resolverType,
    String contextKey
) {}
