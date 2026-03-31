package com.phoenixtask.controlplane.interfaces.dto;

import java.util.List;

public class TenantHealthListResponse {

  private final List<TenantHealthResponse> items;

  public TenantHealthListResponse(List<TenantHealthResponse> items) {
    this.items = items;
  }

  public List<TenantHealthResponse> getItems() {
    return items;
  }
}
