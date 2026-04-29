package com.phoenixtask.projects.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Project(
        Long id,
        String projectKey,
        String name,
        String description,
        String status,
        Long ownerUserId,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
