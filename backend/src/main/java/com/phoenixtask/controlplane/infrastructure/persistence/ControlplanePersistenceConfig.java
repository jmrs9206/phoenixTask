package com.phoenixtask.controlplane.infrastructure.persistence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(
    name = "phoenixtask.controlplane.persistence.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EntityScan(basePackages = "com.phoenixtask.controlplane.infrastructure.persistence")
@EnableJpaRepositories(basePackages = "com.phoenixtask.controlplane.infrastructure.persistence")
public class ControlplanePersistenceConfig {
}
