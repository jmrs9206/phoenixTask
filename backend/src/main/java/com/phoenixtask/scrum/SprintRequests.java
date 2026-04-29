package com.phoenixtask.scrum;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class SprintRequests {
    public record CreateSprintRequest(
        @NotNull @Min(1) Long projectId,
        @NotBlank String name,
        String goal,
        LocalDate startDate,
        LocalDate endDate,
        @Pattern(regexp = "PLANNED|ACTIVE|CLOSED") String status
    ) {
        @AssertTrue(message = "endDate must be after or equal to startDate")
        public boolean isDateRangeValid() {
            if (startDate == null || endDate == null) return true;
            return !endDate.isBefore(startDate);
        }
    }

    public record UpdateSprintRequest(
        String name,
        String goal,
        LocalDate startDate,
        LocalDate endDate,
        @Pattern(regexp = "PLANNED|ACTIVE|CLOSED") String status
    ) {
        @AssertTrue(message = "endDate must be after or equal to startDate")
        public boolean isDateRangeValid() {
            if (startDate == null || endDate == null) return true;
            return !endDate.isBefore(startDate);
        }
    }
}
