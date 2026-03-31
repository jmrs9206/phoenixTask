package com.phoenixtask.publicapi.interfaces.dto;

public record PublicApiIssueResponse(
    Long id,
    String issueKey,
    String title,
    String projectKey,
    String status,
    String priority
) {}
