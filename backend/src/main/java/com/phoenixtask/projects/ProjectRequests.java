package com.phoenixtask.projects;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class ProjectRequests {
    public record CreateProjectRequest(
        @NotBlank 
        @Pattern(regexp = "^[A-Z0-9_-]+$", message = "projectKey must be uppercase letters, numbers or dashes") 
        @Size(min = 2, max = 10) 
        String projectKey,
        
        @NotBlank 
        String name,
        
        String description,
        
        @NotNull 
        @Min(1) 
        Long ownerUserId,
        
        LocalDate plannedStartDate,
        
        LocalDate plannedEndDate
    ) {
        @AssertTrue(message = "plannedEndDate must be after or equal to plannedStartDate")
        public boolean isDateRangeValid() {
            if (plannedStartDate == null || plannedEndDate == null) return true;
            return !plannedEndDate.isBefore(plannedStartDate);
        }
    }

    public record UpdateProjectRequest(
        @Pattern(regexp = "^[A-Z0-9_-]+$", message = "projectKey must be uppercase letters, numbers or dashes") 
        @Size(min = 2, max = 10) 
        String projectKey,
        
        String name,
        
        String description,
        
        @Min(1) 
        Long ownerUserId,
        
        LocalDate plannedStartDate,
        
        LocalDate plannedEndDate
    ) {
        @AssertTrue(message = "plannedEndDate must be after or equal to plannedStartDate")
        public boolean isDateRangeValid() {
            if (plannedStartDate == null || plannedEndDate == null) return true;
            return !plannedEndDate.isBefore(plannedStartDate);
        }
    }

    public record UpdateStatusRequest(
        @NotBlank 
        @Pattern(regexp = "PLANNED|ACTIVE|ON_HOLD|COMPLETED|CANCELLED") 
        String status
    ) {}
}
