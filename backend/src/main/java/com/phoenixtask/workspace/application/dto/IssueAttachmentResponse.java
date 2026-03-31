package com.phoenixtask.workspace.application.dto;

import java.time.LocalDateTime;

public record IssueAttachmentResponse(
    Long id,
    Long issueId,
    Long uploaderId,
    String uploaderName,
    String originalFilename,
    String mimeType,
    Long sizeBytes,
    LocalDateTime createdAt
) {}
