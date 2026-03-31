package com.phoenixtask.publicapi.security;

import com.phoenixtask.publicapi.domain.PublicApiScope;
import org.springframework.http.HttpMethod;

public record PublicApiScopeRule(
    HttpMethod method,
    String pathPattern,
    PublicApiScope scope
) {}
