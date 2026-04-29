package com.phoenixtask.auth.service;

import com.phoenixtask.auth.util.JwtUtil;
import com.phoenixtask.iam.model.PasswordResetToken;
import com.phoenixtask.iam.model.User;
import com.phoenixtask.iam.model.UserInvitation;
import com.phoenixtask.iam.repository.PasswordResetTokenRepository;
import com.phoenixtask.iam.repository.UserInvitationRepository;
import com.phoenixtask.iam.repository.UserRepository;
import com.phoenixtask.shared.error.BadRequestException;
import com.phoenixtask.shared.error.ConflictException;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserInvitationRepository invitationRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, UserInvitationRepository invitationRepository,
                       PasswordResetTokenRepository resetTokenRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!"ACTIVE".equals(user.status())) {
            throw new BadRequestException("User is not active");
        }

        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new BadRequestException("Invalid credentials");
        }

        var roles = userRepository.findRolesByUserId(user.id());
        return jwtUtil.generateToken(user.id(), user.email(), roles);
    }

    public void inviteUser(String email, Long inviterId) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("User already exists");
        }

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hashToken(rawToken);

        User user = new User(null, email, email, "", "INVITED", false, true, null, null, null);
        user = userRepository.save(user);

        UserInvitation invitation = new UserInvitation(null, email, hashedToken, 
                LocalDateTime.now().plusDays(7), null, inviterId, null);
        invitationRepository.save(invitation);

        log.info("Mock Email: Sent invitation to {} with token: {}", email, rawToken);
    }

    public void acceptInvitation(String rawToken, String fullName, String newPassword) {
        String hashedToken = hashToken(rawToken);
        UserInvitation invitation = invitationRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new BadRequestException("Invalid or expired invitation token"));

        if (invitation.usedAt() != null || invitation.expiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invalid or expired invitation token");
        }

        User user = userRepository.findByEmail(invitation.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!"INVITED".equals(user.status())) {
            throw new BadRequestException("User is not in INVITED state");
        }

        User updatedUser = new User(user.id(), user.email(), fullName, 
                passwordEncoder.encode(newPassword), "ACTIVE", user.isPlatformInternal(), 
                false, user.createdAt(), LocalDateTime.now(), LocalDateTime.now());
        
        userRepository.save(updatedUser);

        UserInvitation usedInvitation = new UserInvitation(invitation.id(), invitation.email(), 
                invitation.tokenHash(), invitation.expiresAt(), LocalDateTime.now(), 
                invitation.invitedByUserId(), invitation.createdAt());
        invitationRepository.save(usedInvitation);
    }

    public void requestPasswordReset(String email) {
        // Do not reveal if email exists
        userRepository.findByEmail(email).ifPresent(user -> {
            if ("ACTIVE".equals(user.status())) {
                String rawToken = UUID.randomUUID().toString();
                String hashedToken = hashToken(rawToken);

                PasswordResetToken token = new PasswordResetToken(null, user.id(), hashedToken, 
                        LocalDateTime.now().plusHours(2), null, null);
                resetTokenRepository.save(token);

                log.info("Mock Email: Sent password reset to {} with token: {}", email, rawToken);
            }
        });
    }

    public void confirmPasswordReset(String rawToken, String newPassword) {
        String hashedToken = hashToken(rawToken);
        PasswordResetToken token = resetTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (token.usedAt() != null || token.expiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = userRepository.findById(token.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User updatedUser = new User(user.id(), user.email(), user.displayName(), 
                passwordEncoder.encode(newPassword), user.status(), user.isPlatformInternal(), 
                false, user.createdAt(), LocalDateTime.now(), user.activatedAt());
        
        userRepository.save(updatedUser);

        PasswordResetToken usedToken = new PasswordResetToken(token.id(), token.userId(), 
                token.tokenHash(), token.expiresAt(), LocalDateTime.now(), token.createdAt());
        resetTokenRepository.save(usedToken);
    }
}
