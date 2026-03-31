package com.phoenixtask.controlplane.infrastructure.persistence;

import com.phoenixtask.controlplane.domain.TenantRegistry;

public final class TenantRegistryMapper {

  private TenantRegistryMapper() {}

  public static TenantRegistryEntity toEntity(TenantRegistry domain) {
    TenantRegistryEntity entity = new TenantRegistryEntity();
    entity.setCode(domain.getCode());
    entity.setName(domain.getName());
    entity.setDbHost(domain.getDbHost());
    entity.setDbPort(domain.getDbPort());
    entity.setDbName(domain.getDbName());
    entity.setDbSchema(domain.getDbSchema());
    entity.setDbUsername(domain.getDbUsername());
    entity.setCredentialMode(domain.getCredentialMode());
    entity.setStatus(domain.getStatus());
    return entity;
  }

  public static TenantRegistry toDomain(TenantRegistryEntity entity) {
    TenantRegistry domain = new TenantRegistry();
    domain.setId(entity.getId());
    domain.setCode(entity.getCode());
    domain.setName(entity.getName());
    domain.setDbHost(entity.getDbHost());
    domain.setDbPort(entity.getDbPort());
    domain.setDbName(entity.getDbName());
    domain.setDbSchema(entity.getDbSchema());
    domain.setDbUsername(entity.getDbUsername());
    domain.setCredentialMode(entity.getCredentialMode());
    domain.setCreatedAt(entity.getCreatedAt());
    domain.setUpdatedAt(entity.getUpdatedAt());
    domain.setStatus(entity.getStatus());
    return domain;
  }
}
