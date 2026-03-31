package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.workspace.application.WorkspaceCompanyService;
import com.phoenixtask.workspace.application.dto.CompanyResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace/company")
public class WorkspaceCompanyController {

  private final WorkspaceCompanyService service;

  public WorkspaceCompanyController(WorkspaceCompanyService service) {
    this.service = service;
  }

  @GetMapping
  public CompanyResponse getCompany() {
    return service.getCompany();
  }
}
