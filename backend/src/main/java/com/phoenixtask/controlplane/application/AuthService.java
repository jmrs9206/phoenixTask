package com.phoenixtask.controlplane.application;

import com.phoenixtask.controlplane.infrastructure.config.AuthProperties;
import com.phoenixtask.controlplane.application.audit.AuditDomain;
import com.phoenixtask.controlplane.application.audit.AuditEvent;
import com.phoenixtask.controlplane.application.audit.AuditLogService;
import com.phoenixtask.controlplane.application.audit.AuditOutcome;
import com.phoenixtask.controlplane.infrastructure.persistence.AuthAuditEventEntity;
import com.phoenixtask.controlplane.infrastructure.persistence.AuthAuditEventJpaRepository;
import com.phoenixtask.controlplane.infrastructure.persistence.AuthSessionEntity;
import com.phoenixtask.controlplane.infrastructure.persistence.AuthSessionJpaRepository;
import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.ForbiddenException;
import com.phoenixtask.shared.error.TenantNotFoundException;
import com.phoenixtask.shared.error.UnauthorizedException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.shared.tenant.TenantMetadata;
import com.phoenixtask.shared.tenant.TenantLifecyclePolicy;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceAuthRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceAuthRepository.AuthUserRecord;
import com.phoenixtask.workspace.infrastructure.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private static final String EVENT_LOGIN_SUCCESS = "LOGIN_SUCCESS";
  private static final String EVENT_LOGIN_FAILED = "LOGIN_FAILED";
  private static final String EVENT_LOGOUT = "LOGOUT";

  private final TenantRegistryLookupService tenantLookupService;
  private final WorkspaceAuthRepository workspaceAuthRepository;
  private final AuthSessionJpaRepository authSessionRepository;
  private final AuthAuditEventJpaRepository authAuditRepository;
  private final AuditLogService auditLogService;
  private final ObservabilityMetrics metrics;
  private final PasswordEncoder passwordEncoder;
  private final AuthProperties authProperties;
  private final SecureRandom secureRandom = new SecureRandom();

  public AuthService(
      TenantRegistryLookupService tenantLookupService,
      WorkspaceAuthRepository workspaceAuthRepository,
      AuthSessionJpaRepository authSessionRepository,
      AuthAuditEventJpaRepository authAuditRepository,
      AuditLogService auditLogService,
      ObservabilityMetrics metrics,
      PasswordEncoder passwordEncoder,
      AuthProperties authProperties
  ) {
    this.tenantLookupService = tenantLookupService;
    this.workspaceAuthRepository = workspaceAuthRepository;
    this.authSessionRepository = authSessionRepository;
    this.authAuditRepository = authAuditRepository;
    this.auditLogService = auditLogService;
    this.metrics = metrics;
    this.passwordEncoder = passwordEncoder;
    this.authProperties = authProperties;
  }

  public LoginResult login(
      String tenantCode,
      String email,
      String password,
      HttpServletRequest request
  ) {
    validateLoginRequest(tenantCode, email, password);

    TenantMetadata metadata = tenantLookupService.findMetadataByCode(tenantCode)
        .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));
    TenantLifecyclePolicy.requireActive(metadata.getStatus());

    return withTenantContext(metadata, () -> {
      Optional<AuthUserRecord> candidate = workspaceAuthRepository.findByEmail(email);
      if (candidate.isEmpty()) {
        recordAuditEvent(tenantCode, null, EVENT_LOGIN_FAILED, request, "user_not_found");
        metrics.recordAuthLoginFailure("user_not_found");
        throw new UnauthorizedException("User not found");
      }
      AuthUserRecord user = candidate.get();
      if (!"ACTIVE".equalsIgnoreCase(user.status())) {
        recordAuditEvent(tenantCode, user.id(), EVENT_LOGIN_FAILED, request, "user_inactive");
        metrics.recordAuthLoginFailure("user_inactive");
        throw new ForbiddenException("User is inactive");
      }
      if (!passwordEncoder.matches(password, user.passwordHash())) {
        recordAuditEvent(tenantCode, user.id(), EVENT_LOGIN_FAILED, request, "invalid_password");
        metrics.recordAuthLoginFailure("invalid_password");
        throw new UnauthorizedException("Invalid credentials");
      }

      String token = generateToken(authProperties.getSession().getTokenBytes());
      String tokenHash = sha256Hex(token);
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime expiresAt = now.plusMinutes(authProperties.getSession().getTtlMinutes());

      AuthSessionEntity session = new AuthSessionEntity();
      session.setTokenHash(tokenHash);
      session.setTenantCode(tenantCode);
      session.setUserId(user.id());
      session.setCreatedAt(now);
      session.setExpiresAt(expiresAt);
      authSessionRepository.save(session);

      recordAuditEvent(tenantCode, user.id(), EVENT_LOGIN_SUCCESS, request, null);
      metrics.recordAuthLoginSuccess();

      return new LoginResult(
          "Bearer",
          token,
          expiresAt,
          tenantCode,
          toAuthUser(user)
      );
    });
  }

  public AuthenticatedUser currentUser(String accessToken) {
    AuthSessionEntity session = resolveValidSession(accessToken);
    String tenantCode = session.getTenantCode();

    TenantMetadata metadata = tenantLookupService.findMetadataByCode(tenantCode)
        .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));
    TenantLifecyclePolicy.requireActive(metadata.getStatus());

    return withTenantContext(metadata, () -> {
      AuthUserRecord user = workspaceAuthRepository.findById(session.getUserId())
          .orElseThrow(() -> new UnauthorizedException("User not found"));
      if (!"ACTIVE".equalsIgnoreCase(user.status())) {
        throw new ForbiddenException("User is inactive");
      }
      return new AuthenticatedUser(
          tenantCode,
          session.getExpiresAt(),
          toAuthUser(user)
      );
    });
  }

  public void logout(String accessToken, HttpServletRequest request) {
    AuthSessionEntity session = resolveValidSession(accessToken);
    revokeSession(session, request);
  }

  public void logoutSession(AuthPrincipal principal, HttpServletRequest request) {
    if (principal == null) {
      throw new UnauthorizedException("Session is not valid");
    }
    AuthSessionEntity session = authSessionRepository.findById(principal.getSessionId())
        .orElseThrow(() -> new UnauthorizedException("Session is not valid"));
    if (session.getRevokedAt() == null) {
      if (!session.getUserId().equals(principal.getUserId())
          || !session.getTenantCode().equals(principal.getTenantCode())) {
        throw new UnauthorizedException("Session is not valid");
      }
      revokeSession(session, request);
    }
  }

  public AuthPrincipal authenticate(String accessToken) {
    return authenticateInternal(accessToken, true);
  }

  public AuthPrincipal authenticateAllowInactive(String accessToken) {
    return authenticateInternal(accessToken, false);
  }

  private AuthPrincipal authenticateInternal(String accessToken, boolean requireActiveTenant) {
    AuthSessionEntity session = resolveValidSession(accessToken);
    String tenantCode = session.getTenantCode();

    TenantMetadata metadata = tenantLookupService.findMetadataByCode(tenantCode)
        .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));
    if (requireActiveTenant) {
      TenantLifecyclePolicy.requireActive(metadata.getStatus());
    }

    return withTenantContext(metadata, () -> {
      AuthUserRecord user = workspaceAuthRepository.findById(session.getUserId())
          .orElseThrow(() -> new UnauthorizedException("User not found"));
      if (!"ACTIVE".equalsIgnoreCase(user.status())) {
        throw new ForbiddenException("User is inactive");
      }
      String fullName = buildFullName(user.firstName(), user.lastName());
      return new AuthPrincipal(
          user.id(),
          tenantCode,
          user.email(),
          user.firstName(),
          user.lastName(),
          fullName,
          user.companyId(),
          user.primaryRoleId(),
          user.status(),
          session.getId()
      );
    });
  }

  private AuthSessionEntity resolveValidSession(String accessToken) {
    if (accessToken == null || accessToken.isBlank()) {
      throw new UnauthorizedException("Missing access token");
    }
    cleanupExpiredSessions(LocalDateTime.now());
    String tokenHash = sha256Hex(accessToken);
    LocalDateTime now = LocalDateTime.now();
    return authSessionRepository.findByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(tokenHash, now)
        .orElseThrow(() -> new UnauthorizedException("Session is not valid"));
  }

  private void validateLoginRequest(String tenantCode, String email, String password) {
    if (tenantCode == null || tenantCode.isBlank()) {
      throw new ValidationException("tenantCode is required");
    }
    if (email == null || email.isBlank()) {
      throw new ValidationException("email is required");
    }
    if (password == null || password.isBlank()) {
      throw new ValidationException("password is required");
    }
  }

  private void recordAuditEvent(
      String tenantCode,
      Long userId,
      String eventType,
      HttpServletRequest request,
      String detail
  ) {
    AuthAuditEventEntity event = new AuthAuditEventEntity();
    event.setTenantCode(tenantCode);
    event.setUserId(userId);
    event.setEventType(eventType);
    event.setIpAddress(resolveIpAddress(request));
    event.setUserAgent(resolveUserAgent(request));
    event.setDetail(detail);
    authAuditRepository.save(event);

    AuditOutcome outcome = EVENT_LOGIN_FAILED.equals(eventType)
        ? AuditOutcome.FAILURE
        : AuditOutcome.SUCCESS;
    String requestId = request != null && request.getAttribute("requestId") != null
        ? request.getAttribute("requestId").toString()
        : null;
    AuditEvent auditEvent = new AuditEvent(
        tenantCode,
        AuditDomain.AUTH,
        eventType,
        userId != null ? "USER" : "SYSTEM",
        userId,
        null,
        "SESSION",
        null,
        outcome,
        resolveIpAddress(request),
        resolveUserAgent(request),
        requestId,
        detail
    );
    auditLogService.record(auditEvent);
  }

  private void revokeSession(AuthSessionEntity session, HttpServletRequest request) {
    session.setRevokedAt(LocalDateTime.now());
    authSessionRepository.save(session);
    recordAuditEvent(session.getTenantCode(), session.getUserId(), EVENT_LOGOUT, request, null);
    metrics.recordAuthLogout();
  }

  private void cleanupExpiredSessions(LocalDateTime now) {
    authSessionRepository.deleteByExpiresAtBefore(now);
  }

  private String resolveIpAddress(HttpServletRequest request) {
    if (request == null) {
      return null;
    }
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      String[] parts = forwarded.split(",");
      return parts[0].trim();
    }
    return request.getRemoteAddr();
  }

  private String resolveUserAgent(HttpServletRequest request) {
    if (request == null) {
      return null;
    }
    return request.getHeader("User-Agent");
  }

  private <T> T withTenantContext(TenantMetadata metadata, java.util.concurrent.Callable<T> action) {
    TenantMetadata previous = TenantContext.get();
    TenantContext.set(metadata);
    try {
      return action.call();
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      if (previous != null) {
        TenantContext.set(previous);
      } else {
        TenantContext.clear();
      }
    }
  }

  private AuthUser toAuthUser(AuthUserRecord user) {
    return new AuthUser(
        user.id(),
        user.companyId(),
        user.primaryRoleId(),
        user.firstName(),
        user.lastName(),
        user.email(),
        user.status()
    );
  }

  private String buildFullName(String firstName, String lastName) {
    String safeFirst = firstName == null ? "" : firstName.trim();
    String safeLast = lastName == null ? "" : lastName.trim();
    if (safeFirst.isEmpty()) {
      return safeLast;
    }
    if (safeLast.isEmpty()) {
      return safeFirst;
    }
    return safeFirst + " " + safeLast;
  }

  public LocalDateTime getSessionExpiresAt(Long sessionId) {
    if (sessionId == null) {
      throw new UnauthorizedException("Session is not valid");
    }
    return authSessionRepository.findById(sessionId)
        .map(AuthSessionEntity::getExpiresAt)
        .orElseThrow(() -> new UnauthorizedException("Session is not valid"));
  }


  private String generateToken(int tokenBytes) {
    byte[] bytes = new byte[Math.max(16, tokenBytes)];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String sha256Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        builder.append(String.format("%02x", b));
      }
      return builder.toString();
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to hash token", ex);
    }
  }

  public record LoginResult(
      String tokenType,
      String accessToken,
      LocalDateTime expiresAt,
      String tenantCode,
      AuthUser user
  ) {}

  public record AuthenticatedUser(
      String tenantCode,
      LocalDateTime expiresAt,
      AuthUser user
  ) {}

  public record AuthUser(
      Long userId,
      Long companyId,
      Long primaryRoleId,
      String firstName,
      String lastName,
      String email,
      String status
  ) {}
}
