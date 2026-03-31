package com.phoenixtask.publicapi.interfaces.dto;

public record PublicApiProjectDetailResponse(
    Long id,
    String projectKey,
    String name,
    String status,
    String description
) {}
