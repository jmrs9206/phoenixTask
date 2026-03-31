package com.phoenixtask.controlplane.interfaces;

import com.phoenixtask.controlplane.application.TenantLifecycleService;
import com.phoenixtask.controlplane.application.TenantHealthService;
import com.phoenixtask.controlplane.application.ControlplaneTenantAuthorizationService;
import com.phoenixtask.controlplane.application.TenantProvisioningService;
import com.phoenixtask.controlplane.application.TenantRegistryLookupService;
import com.phoenixtask.controlplane.domain.TenantRegistry;
import com.phoenixtask.controlplane.interfaces.dto.TenantCreateRequest;
import com.phoenixtask.controlplane.interfaces.dto.TenantHealthListResponse;
import com.phoenixtask.controlplane.interfaces.dto.TenantHealthResponse;
import com.phoenixtask.controlplane.interfaces.dto.TenantListResponse;
import com.phoenixtask.controlplane.interfaces.dto.TenantResponse;
import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.security.ControlplaneAdminPrincipal;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.UnauthorizedException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/controlplane/tenants")
@Validated
@ConditionalOnProperty(
    name = "phoenixtask.controlplane.persistence.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class TenantController {

  private final TenantRegistryLookupService lookupService;
  private final TenantProvisioningService provisioningService;
  private final TenantLifecycleService lifecycleService;
  private final TenantHealthService healthService;
  private final ControlplaneTenantAuthorizationService tenantAuthorizationService;

  public TenantController(
      TenantRegistryLookupService lookupService,
      TenantProvisioningService provisioningService,
      TenantLifecycleService lifecycleService,
      TenantHealthService healthService,
      ControlplaneTenantAuthorizationService tenantAuthorizationService
  ) {
    this.lookupService = lookupService;
    this.provisioningService = provisioningService;
    this.lifecycleService = lifecycleService;
    this.healthService = healthService;
    this.tenantAuthorizationService = tenantAuthorizationService;
  }

  @GetMapping
  public TenantListResponse listTenants() {
    Object principal = requirePrincipal();
    tenantAuthorizationService.requireLifecycleManage(principal);
    List<TenantResponse> items = lookupService.findAll().stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
    return new TenantListResponse(items);
  }

  @GetMapping("/{code}")
  public TenantResponse getTenant(@PathVariable String code) {
    Object principal = requirePrincipal();
    tenantAuthorizationService.requireLifecycleManage(principal);
    TenantRegistry tenant = lookupService.findByCode(code)
        .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));
    return toResponse(tenant);
  }

  @GetMapping("/{code}/health")
  public TenantHealthResponse getTenantHealth(@PathVariable String code) {
    Object principal = requirePrincipal();
    tenantAuthorizationService.requireLifecycleManage(principal);
    var snapshot = healthService.healthFor(code);
    return toHealthResponse(snapshot);
  }

  @GetMapping("/health")
  public TenantHealthListResponse listTenantHealth() {
    Object principal = requirePrincipal();
    tenantAuthorizationService.requireLifecycleManage(principal);
    List<TenantHealthResponse> items = healthService.healthAll().stream()
        .map(this::toHealthResponse)
        .collect(Collectors.toList());
    return new TenantHealthListResponse(items);
  }

  @PostMapping
  public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody TenantCreateRequest request) {
    Object principal = requirePrincipal();
    tenantAuthorizationService.requireLifecycleManage(principal);
    TenantRegistry tenant = new TenantRegistry();
    tenant.setCode(request.getCode());
    tenant.setName(request.getName());
    tenant.setDbHost(request.getDbHost());
    tenant.setDbPort(request.getDbPort());
    tenant.setDbName(request.getDbName());
    tenant.setDbSchema(request.getDbSchema());
    tenant.setDbUsername(request.getDbUsername());
    tenant.setCredentialMode("SHARED");
    tenant.setStatus("PENDING");

    TenantRegistry saved = provisioningService.provisionTenant(tenant);
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
  }

  @PostMapping("/{code}/suspend")
  public TenantResponse suspendTenant(@PathVariable String code) {
    Object principal = requirePrincipal();
    tenantAuthorizationService.requireLifecycleManage(principal);
    TenantRegistry tenant = lifecycleService.suspendTenant(code);
    return toResponse(tenant);
  }

  @PostMapping("/{code}/reactivate")
  public TenantResponse reactivateTenant(@PathVariable String code) {
    Object principal = requirePrincipal();
    tenantAuthorizationService.requireLifecycleManage(principal);
    TenantRegistry tenant = lifecycleService.reactivateTenant(code);
    return toResponse(tenant);
  }

  private TenantResponse toResponse(TenantRegistry tenant) {
    return new TenantResponse(
        tenant.getCode(),
        tenant.getName(),
        tenant.getDbHost(),
        tenant.getDbPort(),
        tenant.getDbName(),
        tenant.getDbSchema(),
        tenant.getDbUsername(),
        tenant.getCredentialMode(),
        tenant.getStatus(),
        tenant.getCreatedAt(),
        tenant.getUpdatedAt()
    );
  }

  private TenantHealthResponse toHealthResponse(
      TenantHealthService.TenantHealthSnapshot snapshot
  ) {
    return new TenantHealthResponse(
        snapshot.tenantCode(),
        snapshot.lifecycleStatus(),
        snapshot.credentialMode(),
        snapshot.credentialSource(),
        snapshot.dbConnectivity(),
        snapshot.overallHealth(),
        snapshot.message()
    );
  }

  private Object requirePrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new UnauthorizedException("Missing Authorization header");
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof AuthPrincipal || principal instanceof ControlplaneAdminPrincipal) {
      return principal;
    }
    throw new UnauthorizedException("Missing Authorization header");
  }
}
