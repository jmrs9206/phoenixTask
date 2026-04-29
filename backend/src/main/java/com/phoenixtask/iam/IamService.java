package com.phoenixtask.iam;

import com.phoenixtask.iam.model.User;
import com.phoenixtask.iam.repository.UserRepository;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class IamService {

    private final UserRepository userRepository;

    public IamService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Map<String, Object> getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<String> roles = userRepository.findRolesByUserId(id);
        return Map.of("user", user, "roles", roles);
    }

    @Transactional
    public void updateUserStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User updatedUser = new User(
                user.id(), user.email(), user.displayName(), user.passwordHash(),
                status, user.isPlatformInternal(), user.mustChangePassword(),
                user.createdAt(), user.updatedAt(), user.activatedAt()
        );
        userRepository.save(updatedUser);
    }

    @Transactional
    public void addRole(Long userId, String role) {
        userRepository.addRole(userId, role);
    }

    @Transactional
    public void removeRole(Long userId, String role) {
        userRepository.removeRole(userId, role);
    }
}
