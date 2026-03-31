package com.phoenixtask.controlplane.interfaces.dto;

import java.util.List;

public class TenantListResponse {

  private final List<TenantResponse> items;

  public TenantListResponse(List<TenantResponse> items) {
    this.items = items;
  }

  public List<TenantResponse> getItems() {
    return items;
  }
}
