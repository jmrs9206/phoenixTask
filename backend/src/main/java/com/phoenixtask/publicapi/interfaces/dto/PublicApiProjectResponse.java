package com.phoenixtask.publicapi.interfaces.dto;

public record PublicApiProjectResponse(
    Long id,
    String projectKey,
    String name,
    String status
) {}
