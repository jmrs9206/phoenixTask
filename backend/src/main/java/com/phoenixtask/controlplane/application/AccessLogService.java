package com.phoenixtask.controlplane.application;

import com.phoenixtask.controlplane.infrastructure.persistence.AccessLogEntity;
import com.phoenixtask.controlplane.infrastructure.persistence.AccessLogJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
    name = "phoenixtask.controlplane.persistence.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class AccessLogService {

  private static final Logger log = LoggerFactory.getLogger(AccessLogService.class);

  private final AccessLogJpaRepository repository;

  public AccessLogService(AccessLogJpaRepository repository) {
    this.repository = repository;
  }

  public void record(AccessLogEntity entity) {
    try {
      repository.save(entity);
    } catch (Exception ex) {
      log.warn("Failed to persist access log for {} {}: {}", entity.getHttpMethod(), entity.getPath(), ex.getMessage());
    }
  }
}
