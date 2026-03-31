package com.phoenixtask.controlplane.interfaces.dto;

import java.util.List;

public class AuditLogListResponse {

  private final List<AuditLogResponse> items;

  public AuditLogListResponse(List<AuditLogResponse> items) {
    this.items = items;
  }

  public List<AuditLogResponse> getItems() {
    return items;
  }
}
