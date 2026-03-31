package com.phoenixtask.shared.interfaces;

import com.phoenixtask.controlplane.application.audit.AuditDomain;
import com.phoenixtask.controlplane.application.audit.AuditLogService;
import com.phoenixtask.controlplane.application.audit.AuditOutcome;
import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.ForbiddenException;
import com.phoenixtask.shared.error.MissingTenantHeaderException;
import com.phoenixtask.shared.error.ConflictException;
import com.phoenixtask.shared.error.ProvisioningException;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.TenantInactiveException;
import com.phoenixtask.shared.error.TenantNotFoundException;
import com.phoenixtask.shared.error.UnauthorizedException;
import com.phoenixtask.shared.error.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {

  private final AuditLogService auditLogService;

  public ApiErrorHandler(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @ExceptionHandler(MissingTenantHeaderException.class)
  public ResponseEntity<ApiErrorResponse> handleMissingTenant(
      MissingTenantHeaderException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.BAD_REQUEST, "TENANT_HEADER_MISSING", ex.getMessage(), request);
  }

  @ExceptionHandler(TenantNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleTenantNotFound(
      TenantNotFoundException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.NOT_FOUND, "TENANT_NOT_FOUND", ex.getMessage(), request);
  }

  @ExceptionHandler(TenantInactiveException.class)
  public ResponseEntity<ApiErrorResponse> handleTenantInactive(
      TenantInactiveException ex,
      HttpServletRequest request
  ) {
    auditAccessDenied(request, "TENANT_INACTIVE", ex.getMessage());
    return build(HttpStatus.CONFLICT, "TENANT_INACTIVE", ex.getMessage(), request);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(
      ValidationException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiErrorResponse> handleConflict(
      ConflictException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), request);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiErrorResponse> handleForbidden(
      ForbiddenException ex,
      HttpServletRequest request
  ) {
    auditAccessDenied(request, "ACCESS_FORBIDDEN", ex.getMessage());
    return build(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), request);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiErrorResponse> handleUnauthorized(
      UnauthorizedException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpServletRequest request
  ) {
    String message = "Validation failed";
    FieldError fieldError = ex.getBindingResult().getFieldError();
    if (fieldError != null) {
      message = fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
      ResourceNotFoundException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
  }

  @ExceptionHandler(ProvisioningException.class)
  public ResponseEntity<ApiErrorResponse> handleProvisioning(
      ProvisioningException ex,
      HttpServletRequest request
  ) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "PROVISIONING_FAILED", ex.getMessage(), request);
  }

  private ResponseEntity<ApiErrorResponse> build(
      HttpStatus status,
      String error,
      String message,
      HttpServletRequest request
  ) {
    ApiErrorResponse body = new ApiErrorResponse(
        OffsetDateTime.now().toString(),
        status.value(),
        error,
        message,
        request.getRequestURI()
    );
    return ResponseEntity.status(status).body(body);
  }

  private void auditAccessDenied(HttpServletRequest request, String eventType, String detail) {
    AuthPrincipal principal = resolvePrincipal();
    String tenantCode = principal != null ? principal.getTenantCode() : request.getHeader("X-Tenant-Code");
    auditLogService.recordFromRequest(
        AuditDomain.SECURITY,
        eventType,
        tenantCode,
        principal,
        "REQUEST",
        request.getRequestURI(),
        AuditOutcome.DENIED,
        detail,
        request
    );
  }

  private AuthPrincipal resolvePrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof AuthPrincipal principal) {
      return principal;
    }
    return null;
  }
}
