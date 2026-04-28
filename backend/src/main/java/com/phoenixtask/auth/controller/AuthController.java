package com.phoenixtask.auth.controller;

import com.phoenixtask.auth.service.AuthService;
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

    record LoginRequest(String email, String password) {}
    record TokenResponse(String token) {}

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    record InviteRequest(String email, Long inviterId) {}

    @PostMapping("/invite")
    public ResponseEntity<Void> inviteUser(@RequestBody InviteRequest request) {
        // In a real app, inviterId comes from security context
        authService.inviteUser(request.email(), request.inviterId());
        return ResponseEntity.ok().build();
    }

    record AcceptInviteRequest(String token, String newPassword) {}

    @PostMapping("/accept-invite")
    public ResponseEntity<Void> acceptInvitation(@RequestBody AcceptInviteRequest request) {
        authService.acceptInvitation(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    record RequestResetRequest(String email) {}

    @PostMapping("/request-reset")
    public ResponseEntity<Void> requestReset(@RequestBody RequestResetRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.ok().build();
    }

    record ConfirmResetRequest(String token, String newPassword) {}

    @PostMapping("/confirm-reset")
    public ResponseEntity<Void> confirmReset(@RequestBody ConfirmResetRequest request) {
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
