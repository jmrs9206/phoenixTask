package com.phoenixtask.workspace.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IssueCommentCreateRequest(
    @NotBlank
    @Size(max = 2000)
    String body
) {}
