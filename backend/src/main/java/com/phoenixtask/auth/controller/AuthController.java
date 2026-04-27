package com.phoenixtask.auth.controller;

import com.phoenixtask.auth.service.AuthService;
import com.phoenixtask.auth.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
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
    public ResponseEntity<Map<String, Object>> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            String email = jwtUtil.getEmailFromToken(token);
            return ResponseEntity.ok(Map.of("id", userId, "email", email));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}
