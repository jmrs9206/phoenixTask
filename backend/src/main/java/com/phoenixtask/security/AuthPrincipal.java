package com.phoenixtask.security;

import java.security.Principal;

public class AuthPrincipal implements Principal {

  private final Long userId;
  private final String tenantCode;
  private final String email;
  private final String firstName;
  private final String lastName;
  private final String fullName;
  private final Long companyId;
  private final Long primaryRoleId;
  private final String status;
  private final Long sessionId;

  public AuthPrincipal(
      Long userId,
      String tenantCode,
      String email,
      String firstName,
      String lastName,
      String fullName,
      Long companyId,
      Long primaryRoleId,
      String status,
      Long sessionId
  ) {
    this.userId = userId;
    this.tenantCode = tenantCode;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.fullName = fullName;
    this.companyId = companyId;
    this.primaryRoleId = primaryRoleId;
    this.status = status;
    this.sessionId = sessionId;
  }

  public Long getUserId() {
    return userId;
  }

  public String getTenantCode() {
    return tenantCode;
  }

  public String getEmail() {
    return email;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getFullName() {
    return fullName;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public Long getPrimaryRoleId() {
    return primaryRoleId;
  }

  public String getStatus() {
    return status;
  }

  public Long getSessionId() {
    return sessionId;
  }

  @Override
  public String getName() {
    return email;
  }
}
