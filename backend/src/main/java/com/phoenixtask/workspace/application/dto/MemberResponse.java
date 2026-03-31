package com.phoenixtask.workspace.application.dto;

public record MemberResponse(
    Long userId,
    String fullName,
    String email,
    String roleCode,
    String roleName,
    String status
) {}
