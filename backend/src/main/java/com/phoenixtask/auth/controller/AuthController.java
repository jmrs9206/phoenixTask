package com.phoenixtask.auth.controller;

import com.phoenixtask.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}
    public record TokenResponse(String token) {}

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    public record InviteRequest(
        @NotBlank @Email String email,
        @NotNull Long inviterId
    ) {}

    @PostMapping("/invite")
    public ResponseEntity<Void> inviteUser(@Valid @RequestBody InviteRequest request) {
        // In a real app, inviterId comes from security context
        authService.inviteUser(request.email(), request.inviterId());
        return ResponseEntity.ok().build();
    }

    public record AcceptInviteRequest(
        @NotBlank String token,
        @NotBlank String fullName,
        @NotBlank @Size(min = 8) String password
    ) {}

    @PostMapping("/accept-invite")
    public ResponseEntity<Void> acceptInvitation(@Valid @RequestBody AcceptInviteRequest request) {
        authService.acceptInvitation(request.token(), request.fullName(), request.password());
        return ResponseEntity.ok().build();
    }

    public record RequestResetRequest(
        @NotBlank @Email String email
    ) {}

    @PostMapping("/request-reset")
    public ResponseEntity<Void> requestReset(@Valid @RequestBody RequestResetRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok().build();
    }

    public record ConfirmResetRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8) String newPassword
    ) {}

    @PostMapping("/confirm-reset")
    public ResponseEntity<Void> confirmReset(@Valid @RequestBody ConfirmResetRequest request) {
        authService.confirmPasswordReset(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(Map.of(
            "email", authentication.getName(),
            "roles", authentication.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .toList()
        ));
    }
}
