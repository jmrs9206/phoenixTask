package com.phoenixtask.security;

public record RateLimitPolicy(
    String name,
    String pathPattern,
    int capacity,
    int windowSeconds,
    RateLimitKeyStrategy keyStrategy
) {}
