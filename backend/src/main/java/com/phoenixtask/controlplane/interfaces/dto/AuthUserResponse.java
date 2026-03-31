package com.phoenixtask.controlplane.interfaces.dto;

public class AuthUserResponse {

  private final Long userId;
  private final Long companyId;
  private final Long primaryRoleId;
  private final String firstName;
  private final String lastName;
  private final String email;
  private final String status;

  public AuthUserResponse(
      Long userId,
      Long companyId,
      Long primaryRoleId,
      String firstName,
      String lastName,
      String email,
      String status
  ) {
    this.userId = userId;
    this.companyId = companyId;
    this.primaryRoleId = primaryRoleId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.status = status;
  }

  public Long getUserId() {
    return userId;
  }

  public Long getCompanyId() {
    return companyId;
  }

  public Long getPrimaryRoleId() {
    return primaryRoleId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getStatus() {
    return status;
  }
}
