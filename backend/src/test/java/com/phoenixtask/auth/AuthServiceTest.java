package com.phoenixtask.auth;

import com.phoenixtask.auth.service.AuthService;
import com.phoenixtask.auth.util.JwtUtil;
import com.phoenixtask.iam.model.PasswordResetToken;
import com.phoenixtask.iam.model.User;
import com.phoenixtask.iam.model.UserInvitation;
import com.phoenixtask.iam.repository.PasswordResetTokenRepository;
import com.phoenixtask.iam.repository.UserInvitationRepository;
import com.phoenixtask.iam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private UserInvitationRepository invitationRepository;
    private PasswordResetTokenRepository resetTokenRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        invitationRepository = mock(UserInvitationRepository.class);
        resetTokenRepository = mock(PasswordResetTokenRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        jwtUtil = mock(JwtUtil.class);
        authService = new AuthService(userRepository, invitationRepository, resetTokenRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void login_Success() {
        String hash = passwordEncoder.encode("password");
        User user = new User(1L, "test@test.com", "Test", hash, "ACTIVE", false, false, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(1L, "test@test.com")).thenReturn("token");

        String result = authService.login("test@test.com", "password");
        assertEquals("token", result);
    }

    @Test
    void inviteUser_Success() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(i -> {
            User u = i.getArgument(0);
            return new User(2L, u.email(), u.displayName(), u.passwordHash(), u.status(), u.isPlatformInternal(), u.mustChangePassword(), null, null, null);
        });

        authService.inviteUser("new@test.com", 1L);

        verify(userRepository).save(any(User.class));
        verify(invitationRepository).save(any(UserInvitation.class));
    }

    @Test
    void acceptInvitation_Success() {
        // Hash for "token123"
        UserInvitation inv = new UserInvitation(1L, "new@test.com", 
                "a021cd5eab9b17ad3d8d6dc6b0e895ec35b91b7d8d21c33a9254c256ab5f1595", // SHA-256 for "token123" approx, mocked find anyway
                LocalDateTime.now().plusDays(1), null, 1L, LocalDateTime.now());
        
        when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.of(inv));
        User user = new User(2L, "new@test.com", "New", "", "INVITED", false, true, LocalDateTime.now(), null, null);
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.of(user));

        authService.acceptInvitation("token123", "newPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertEquals("ACTIVE", savedUser.status());
        assertTrue(passwordEncoder.matches("newPassword", savedUser.passwordHash()));
        
        ArgumentCaptor<UserInvitation> invCaptor = ArgumentCaptor.forClass(UserInvitation.class);
        verify(invitationRepository).save(invCaptor.capture());
        assertNotNull(invCaptor.getValue().usedAt());
    }

    @Test
    void acceptInvitation_Expired_Throws() {
        UserInvitation inv = new UserInvitation(1L, "new@test.com", "hash", 
                LocalDateTime.now().minusDays(1), null, 1L, LocalDateTime.now());
        when(invitationRepository.findByTokenHash(anyString())).thenReturn(Optional.of(inv));

        assertThrows(RuntimeException.class, () -> authService.acceptInvitation("token123", "pass"));
    }

    @Test
    void confirmPasswordReset_Success() {
        PasswordResetToken token = new PasswordResetToken(1L, 2L, "hash", 
                LocalDateTime.now().plusHours(1), null, LocalDateTime.now());
        when(resetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        
        User user = new User(2L, "test@test.com", "Test", "oldhash", "ACTIVE", false, false, LocalDateTime.now(), null, null);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        authService.confirmPasswordReset("token123", "newPass");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(passwordEncoder.matches("newPass", userCaptor.getValue().passwordHash()));
    }
}
