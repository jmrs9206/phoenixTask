package com.phoenixtask.workspace.interfaces;

import com.phoenixtask.workspace.application.WorkspaceAnalyticsService;
import com.phoenixtask.workspace.application.dto.AnalyticsAdvancedResponse;
import com.phoenixtask.workspace.application.dto.AnalyticsSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace/analytics")
public class WorkspaceAnalyticsController {

  private final WorkspaceAnalyticsService analyticsService;

  public WorkspaceAnalyticsController(WorkspaceAnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  @GetMapping("/summary")
  public AnalyticsSummaryResponse summary() {
    return analyticsService.getSummary();
  }

  @GetMapping("/advanced")
  public AnalyticsAdvancedResponse advanced() {
    return analyticsService.getAdvanced();
  }
}
