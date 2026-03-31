package com.phoenixtask.workspace.domain;

import java.time.LocalDateTime;

public class Role {

  private Long id;
  private Long companyId;
  private String code;
  private String name;
  private String description;
  private boolean internal;
  private boolean assignable;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Long companyId) {
    this.companyId = companyId;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isInternal() {
    return internal;
  }

  public void setInternal(boolean internal) {
    this.internal = internal;
  }

  public boolean isAssignable() {
    return assignable;
  }

  public void setAssignable(boolean assignable) {
    this.assignable = assignable;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
