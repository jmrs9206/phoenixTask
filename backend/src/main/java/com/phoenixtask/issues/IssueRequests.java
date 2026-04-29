package com.phoenixtask.issues;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class IssueRequests {
    public record CreateIssueRequest(
        @NotNull @Min(1) Long projectId,
        @NotBlank String title,
        String description,
        @NotNull @Min(1) Long reporterUserId,
        @Min(1) Long assigneeUserId,
        @Pattern(regexp = "BACKLOG|TODO|IN_PROGRESS|DONE|CLOSED") String status,
        @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT") String priority,
        LocalDate plannedStartDate,
        LocalDate dueDate
    ) {
        @AssertTrue(message = "dueDate must be after or equal to plannedStartDate")
        public boolean isDateRangeValid() {
            if (plannedStartDate == null || dueDate == null) return true;
            return !dueDate.isBefore(plannedStartDate);
        }
    }

    public record UpdateIssueRequest(
        @NotBlank String title,
        String description,
        @Min(1) Long assigneeUserId,
        @Pattern(regexp = "BACKLOG|TODO|IN_PROGRESS|DONE|CLOSED") String status,
        @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT") String priority,
        LocalDate plannedStartDate,
        LocalDate dueDate
    ) {
        @AssertTrue(message = "dueDate must be after or equal to plannedStartDate")
        public boolean isDateRangeValid() {
            if (plannedStartDate == null || dueDate == null) return true;
            return !dueDate.isBefore(plannedStartDate);
        }
    }

    public record UpdateStatusRequest(
        @NotBlank @Pattern(regexp = "BACKLOG|TODO|IN_PROGRESS|DONE|CLOSED") String status
    ) {}
}
