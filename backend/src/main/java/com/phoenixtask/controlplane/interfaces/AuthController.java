package com.phoenixtask.controlplane.interfaces;

import com.phoenixtask.controlplane.application.AuthService;
import com.phoenixtask.controlplane.interfaces.dto.AuthLoginRequest;
import com.phoenixtask.controlplane.interfaces.dto.AuthLoginResponse;
import com.phoenixtask.controlplane.interfaces.dto.AuthMeResponse;
import com.phoenixtask.controlplane.interfaces.dto.AuthUserResponse;
import com.phoenixtask.security.AuthPrincipal;
import com.phoenixtask.shared.error.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public AuthLoginResponse login(
      @Valid @RequestBody AuthLoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  ) {
    AuthService.LoginResult result = authService.login(
        request.getTenantCode(),
        request.getEmail(),
        request.getPassword(),
        httpRequest
    );
    setSessionCookie(httpRequest, httpResponse, result.accessToken(), result.expiresAt());
    return new AuthLoginResponse(
        result.tokenType(),
        result.accessToken(),
        result.expiresAt(),
        result.tenantCode(),
        toUserResponse(result.user())
    );
  }

  @GetMapping("/me")
  public AuthMeResponse me(
      HttpServletRequest httpRequest
  ) {
    AuthPrincipal principal = requirePrincipal();
    return new AuthMeResponse(
        principal.getTenantCode(),
        authService.getSessionExpiresAt(principal.getSessionId()),
        new AuthUserResponse(
            principal.getUserId(),
            principal.getCompanyId(),
            principal.getPrimaryRoleId(),
            principal.getFirstName(),
            principal.getLastName(),
            principal.getEmail(),
            principal.getStatus()
        )
    );
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  ) {
    AuthPrincipal principal = requirePrincipal();
    authService.logoutSession(principal, httpRequest);
    clearSessionCookie(httpRequest, httpResponse);
    return ResponseEntity.noContent().build();
  }

  private AuthPrincipal requirePrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
      throw new UnauthorizedException("Missing Authorization header");
    }
    return principal;
  }

  private AuthUserResponse toUserResponse(AuthService.AuthUser user) {
    return new AuthUserResponse(
        user.userId(),
        user.companyId(),
        user.primaryRoleId(),
        user.firstName(),
        user.lastName(),
        user.email(),
        user.status()
    );
  }

  private void setSessionCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String token,
      LocalDateTime expiresAt
  ) {
    long maxAge = Math.max(0, Duration.between(LocalDateTime.now(), expiresAt).getSeconds());
    ResponseCookie cookie = ResponseCookie.from("phoenixtask_auth", token)
        .httpOnly(true)
        .secure(request.isSecure())
        .path("/")
        .sameSite("Lax")
        .maxAge(maxAge)
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }

  private void clearSessionCookie(HttpServletRequest request, HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from("phoenixtask_auth", "")
        .httpOnly(true)
        .secure(request.isSecure())
        .path("/")
        .sameSite("Lax")
        .maxAge(0)
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }
}
