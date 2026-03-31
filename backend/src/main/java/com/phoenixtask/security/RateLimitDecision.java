package com.phoenixtask.security;

public record RateLimitDecision(
    boolean allowed,
    int remaining,
    long resetEpochSeconds,
    int retryAfterSeconds
) {}
