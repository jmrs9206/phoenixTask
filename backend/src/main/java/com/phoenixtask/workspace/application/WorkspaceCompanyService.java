package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.workspace.application.dto.CompanyResponse;
import com.phoenixtask.workspace.application.dto.CompanySettingsResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceCompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceCompanyService {

  private final WorkspaceCompanyRepository repository;

  public WorkspaceCompanyService(WorkspaceCompanyRepository repository) {
    this.repository = repository;
  }

  public CompanyResponse getCompany() {
    WorkspaceCompanyRepository.CompanyWithSettingsRow row = repository.fetchCompanyWithSettings()
        .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

    CompanySettingsResponse settings = new CompanySettingsResponse(
        row.settingsId(),
        row.settingsTimezone(),
        row.settingsLocale(),
        row.settingsWeekStart(),
        row.settingsCreatedAt(),
        row.settingsUpdatedAt()
    );

    return new CompanyResponse(
        row.companyId(),
        row.companyCode(),
        row.companyName(),
        row.companyStatus(),
        row.ownerUserId(),
        row.companyCreatedAt(),
        row.companyUpdatedAt(),
        settings
    );
  }

  public Long getCompanyId() {
    Long companyId = repository.getCompanyId();
    if (companyId == null) {
      throw new com.phoenixtask.shared.error.ResourceNotFoundException("Company not found");
    }
    return companyId;
  }
}
