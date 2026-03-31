package com.phoenixtask.controlplane.application;

import com.phoenixtask.controlplane.infrastructure.persistence.ControlplaneAdminKeyEntity;
import com.phoenixtask.controlplane.infrastructure.persistence.ControlplaneAdminKeyJpaRepository;
import com.phoenixtask.shared.error.UnauthorizedException;
import com.phoenixtask.shared.security.SecretHashingService;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ControlplaneAdminKeyService {

  private static final String STATUS_ACTIVE = "ACTIVE";
  private static final String STATUS_REVOKED = "REVOKED";

  private final ControlplaneAdminKeyJpaRepository repository;
  private final SecretHashingService hashingService;

  public ControlplaneAdminKeyService(
      ControlplaneAdminKeyJpaRepository repository,
      SecretHashingService hashingService
  ) {
    this.repository = repository;
    this.hashingService = hashingService;
  }

  public ControlplaneAdminKeyRecord authenticate(String rawKey) {
    if (rawKey == null || rawKey.isBlank()) {
      throw new UnauthorizedException("Missing controlplane admin key");
    }
    Optional<String> hashed = hashingService.hmacHex(rawKey);
    ControlplaneAdminKeyEntity entity = hashed
        .flatMap(repository::findByKeyHash)
        .orElseGet(() -> repository.findByKeyHash(hashingService.sha256Hex(rawKey))
            .orElseThrow(() -> new UnauthorizedException("Invalid controlplane admin key")));
    upgradeHashIfNeeded(entity, hashed);
    if (!STATUS_ACTIVE.equalsIgnoreCase(entity.getStatus())) {
      throw new UnauthorizedException("Controlplane admin key revoked");
    }
    return toRecord(entity);
  }

  public void ensureBootstrapKey(String rawKey, String label) {
    if (rawKey == null || rawKey.isBlank()) {
      return;
    }
    String normalizedLabel = (label == null || label.isBlank()) ? "bootstrap" : label.trim();
    String hash = hashingService.hashHex(rawKey);
    if (repository.findByKeyHash(hash).isPresent()) {
      return;
    }
    ControlplaneAdminKeyEntity entity = new ControlplaneAdminKeyEntity();
    entity.setLabel(normalizedLabel);
    entity.setKeyPrefix(rawKey.substring(0, Math.min(12, rawKey.length())));
    entity.setKeyHash(hash);
    entity.setStatus(STATUS_ACTIVE);
    entity.setCreatedAt(OffsetDateTime.now());
    entity.setRevokedAt(null);
    repository.save(entity);
  }

  private ControlplaneAdminKeyRecord toRecord(ControlplaneAdminKeyEntity entity) {
    return new ControlplaneAdminKeyRecord(
        entity.getId(),
        entity.getLabel(),
        entity.getKeyPrefix(),
        entity.getStatus(),
        entity.getCreatedAt(),
        entity.getRevokedAt()
    );
  }

  private void upgradeHashIfNeeded(ControlplaneAdminKeyEntity entity, Optional<String> preferredHash) {
    if (preferredHash.isEmpty()) {
      return;
    }
    String current = entity.getKeyHash();
    String target = preferredHash.get();
    if (!target.equals(current)) {
      entity.setKeyHash(target);
      repository.save(entity);
    }
  }
}
