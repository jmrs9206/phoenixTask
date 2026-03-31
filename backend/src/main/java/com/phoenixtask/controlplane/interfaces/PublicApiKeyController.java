package com.phoenixtask.controlplane.interfaces;

import com.phoenixtask.controlplane.application.ControlplanePublicApiAuthorizationService;
import com.phoenixtask.controlplane.interfaces.dto.PublicApiKeyCreateRequest;
import com.phoenixtask.controlplane.interfaces.dto.PublicApiKeyCreateResponse;
import com.phoenixtask.controlplane.interfaces.dto.PublicApiKeyListResponse;
import com.phoenixtask.controlplane.interfaces.dto.PublicApiKeyResponse;
import com.phoenixtask.controlplane.interfaces.dto.PublicApiKeyRevokeResponse;
import com.phoenixtask.publicapi.application.PublicApiKeyService;
import com.phoenixtask.publicapi.domain.PublicApiKeyRecord;
import com.phoenixtask.security.AuthPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/controlplane/public-api/keys")
public class PublicApiKeyController {

  private final PublicApiKeyService keyService;
  private final ControlplanePublicApiAuthorizationService authorizationService;

  public PublicApiKeyController(
      PublicApiKeyService keyService,
      ControlplanePublicApiAuthorizationService authorizationService
  ) {
    this.keyService = keyService;
    this.authorizationService = authorizationService;
  }

  @PostMapping
  public PublicApiKeyCreateResponse create(@Valid @RequestBody PublicApiKeyCreateRequest request) {
    AuthPrincipal principal = requirePrincipal();
    authorizationService.requireManageKeys(principal);
    PublicApiKeyService.PublicApiKeyCreateResult result =
        keyService.createKey(principal.getTenantCode(), request.label(), request.scopes());
    PublicApiKeyRecord record = result.record();
    return new PublicApiKeyCreateResponse(
        record.id(),
        record.tenantCode(),
        record.label(),
        record.keyPrefix(),
        result.token(),
        record.scopes(),
        record.status().name(),
        record.createdAt()
    );
  }

  @GetMapping
  public PublicApiKeyListResponse list() {
    AuthPrincipal principal = requirePrincipal();
    authorizationService.requireManageKeys(principal);
    List<PublicApiKeyResponse> keys = keyService.listKeys(principal.getTenantCode()).stream()
        .map(record -> new PublicApiKeyResponse(
            record.id(),
            record.tenantCode(),
            record.label(),
            record.keyPrefix(),
            record.scopes(),
            record.status().name(),
            record.createdAt(),
            record.revokedAt()
        ))
        .toList();
    return new PublicApiKeyListResponse(keys);
  }

  @PostMapping("/{keyId}/revoke")
  public PublicApiKeyRevokeResponse revoke(@PathVariable Long keyId) {
    AuthPrincipal principal = requirePrincipal();
    authorizationService.requireManageKeys(principal);
    PublicApiKeyRecord record = keyService.revokeKey(principal.getTenantCode(), keyId);
    return new PublicApiKeyRevokeResponse(record.id(), record.status().name(), record.revokedAt());
  }

  private AuthPrincipal requirePrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
      throw new com.phoenixtask.shared.error.UnauthorizedException("Missing Authorization header");
    }
    return principal;
  }
}
