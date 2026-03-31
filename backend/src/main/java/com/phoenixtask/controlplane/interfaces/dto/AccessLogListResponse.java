package com.phoenixtask.controlplane.interfaces.dto;

import java.util.List;

public class AccessLogListResponse {

  private final List<AccessLogResponse> items;

  public AccessLogListResponse(List<AccessLogResponse> items) {
    this.items = items;
  }

  public List<AccessLogResponse> getItems() {
    return items;
  }
}
