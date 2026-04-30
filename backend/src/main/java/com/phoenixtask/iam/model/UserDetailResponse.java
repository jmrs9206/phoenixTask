package com.phoenixtask.iam.model;

import java.util.List;

public record UserDetailResponse(
    Long id,
    String email,
    String displayName,
    String status,
    List<String> roles
) {}
