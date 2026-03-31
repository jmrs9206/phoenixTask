package com.phoenixtask.controlplane.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnProperty(
    name = "phoenixtask.controlplane.persistence.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class ControlplaneAdminKeyBootstrap {

  private static final Logger log = LoggerFactory.getLogger(ControlplaneAdminKeyBootstrap.class);

  private final ControlplaneAdminKeyService adminKeyService;
  private final String bootstrapKey;
  private final String bootstrapLabel;

  public ControlplaneAdminKeyBootstrap(
      ControlplaneAdminKeyService adminKeyService,
      @Value("${phoenixtask.controlplane.admin.bootstrap-key:}") String bootstrapKey,
      @Value("${phoenixtask.controlplane.admin.bootstrap-label:bootstrap}") String bootstrapLabel
  ) {
    this.adminKeyService = adminKeyService;
    this.bootstrapKey = bootstrapKey;
    this.bootstrapLabel = bootstrapLabel;
  }

  @PostConstruct
  public void ensureBootstrapKey() {
    if (bootstrapKey == null || bootstrapKey.isBlank()) {
      log.warn("No controlplane admin bootstrap key configured. Reactivation requires admin key.");
      return;
    }
    adminKeyService.ensureBootstrapKey(bootstrapKey, bootstrapLabel);
    log.info("Controlplane admin bootstrap key ensured (label={})", bootstrapLabel);
  }
}
