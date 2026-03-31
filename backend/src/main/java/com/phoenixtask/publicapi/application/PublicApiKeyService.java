package com.phoenixtask.publicapi.application;

import com.phoenixtask.controlplane.infrastructure.persistence.PublicApiKeyEntity;
import com.phoenixtask.controlplane.infrastructure.persistence.PublicApiKeyJpaRepository;
import com.phoenixtask.publicapi.domain.PublicApiKeyRecord;
import com.phoenixtask.publicapi.domain.PublicApiKeyStatus;
import com.phoenixtask.publicapi.domain.PublicApiScope;
import com.phoenixtask.shared.error.UnauthorizedException;
import com.phoenixtask.shared.error.ValidationException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.phoenixtask.shared.security.SecretHashingService;
import org.springframework.stereotype.Service;

@Service
public class PublicApiKeyService {

  private final PublicApiKeyJpaRepository repository;
  private final SecretHashingService hashingService;
  private final SecureRandom random = new SecureRandom();

  public PublicApiKeyService(
      PublicApiKeyJpaRepository repository,
      SecretHashingService hashingService
  ) {
    this.repository = repository;
    this.hashingService = hashingService;
  }

  public PublicApiKeyCreateResult createKey(String tenantCode, String label, Set<String> scopes) {
    if (tenantCode == null || tenantCode.isBlank()) {
      throw new ValidationException("tenantCode is required");
    }
    if (label == null || label.isBlank()) {
      throw new ValidationException("label is required");
    }
    if (scopes == null || scopes.isEmpty()) {
      throw new ValidationException("scopes are required");
    }
    validateScopes(scopes);

    String token = generateToken();
    String hash = hashingService.hashHex(token);
    String prefix = token.substring(0, Math.min(10, token.length()));

    PublicApiKeyEntity entity = new PublicApiKeyEntity();
    entity.setTenantCode(tenantCode);
    entity.setLabel(label.trim());
    entity.setKeyPrefix(prefix);
    entity.setKeyHash(hash);
    entity.setScopes(String.join(",", scopes));
    entity.setStatus(PublicApiKeyStatus.ACTIVE.name());
    entity.setCreatedAt(OffsetDateTime.now());
    entity.setRevokedAt(null);

    PublicApiKeyEntity saved = repository.save(entity);
    return new PublicApiKeyCreateResult(toRecord(saved), token);
  }

  public PublicApiKeyRecord authenticate(String token) {
    if (token == null || token.isBlank()) {
      throw new UnauthorizedException("Missing public API key");
    }
    Optional<String> hashed = hashingService.hmacHex(token);
    PublicApiKeyEntity entity = hashed
        .flatMap(repository::findByKeyHash)
        .orElseGet(() -> repository.findByKeyHash(hashingService.sha256Hex(token))
            .orElseThrow(() -> new UnauthorizedException("Invalid public API key")));
    upgradeHashIfNeeded(entity, hashed);
    PublicApiKeyStatus status = PublicApiKeyStatus.valueOf(entity.getStatus());
    if (status != PublicApiKeyStatus.ACTIVE) {
      throw new UnauthorizedException("Public API key revoked");
    }
    return toRecord(entity);
  }

  public List<PublicApiKeyRecord> listKeys(String tenantCode) {
    return repository.findByTenantCodeOrderByCreatedAtDesc(tenantCode).stream()
        .map(this::toRecord)
        .toList();
  }

  public PublicApiKeyRecord revokeKey(String tenantCode, Long keyId) {
    PublicApiKeyEntity entity = repository.findById(keyId)
        .orElseThrow(() -> new ValidationException("API key not found"));
    if (!entity.getTenantCode().equals(tenantCode)) {
      throw new UnauthorizedException("API key does not belong to tenant");
    }
    if (!PublicApiKeyStatus.REVOKED.name().equals(entity.getStatus())) {
      entity.setStatus(PublicApiKeyStatus.REVOKED.name());
      entity.setRevokedAt(OffsetDateTime.now());
    }
    return toRecord(repository.save(entity));
  }

  private void validateScopes(Set<String> scopes) {
    Set<String> valid = List.of(PublicApiScope.values()).stream()
        .map(PublicApiScope::code)
        .collect(Collectors.toSet());
    for (String scope : scopes) {
      if (!valid.contains(scope)) {
        throw new ValidationException("Unknown scope: " + scope);
      }
    }
  }

  private PublicApiKeyRecord toRecord(PublicApiKeyEntity entity) {
    Set<String> scopeSet = List.of(entity.getScopes().split(",")).stream()
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .collect(Collectors.toSet());
    return new PublicApiKeyRecord(
        entity.getId(),
        entity.getTenantCode(),
        entity.getLabel(),
        entity.getKeyPrefix(),
        scopeSet,
        PublicApiKeyStatus.valueOf(entity.getStatus()),
        entity.getCreatedAt(),
        entity.getRevokedAt()
    );
  }

  private String generateToken() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return "ptk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private void upgradeHashIfNeeded(PublicApiKeyEntity entity, Optional<String> preferredHash) {
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

  public record PublicApiKeyCreateResult(PublicApiKeyRecord record, String token) {}
}
