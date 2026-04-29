package com.phoenixtask.kanban;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class KanbanRequests {
    public record MoveRequest(
        @NotNull(message = "Project ID is required")
        @Min(value = 1, message = "Invalid Project ID")
        Long projectId,

        @NotBlank(message = "Target status is required")
        @Pattern(regexp = "BACKLOG|TODO|IN_PROGRESS|IN_REVIEW|DONE", message = "Invalid status")
        String targetStatus,

        @Min(value = 0, message = "Target index must be at least 0")
        int targetIndex
    ) {}
}
