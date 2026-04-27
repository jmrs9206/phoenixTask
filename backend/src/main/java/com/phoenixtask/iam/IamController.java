package com.phoenixtask.iam;

import com.phoenixtask.iam.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iam")
public class IamController {

    private final IamService iamService;

    public IamController(IamService iamService) {
        this.iamService = iamService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('platform_owner')")
    public List<User> listUsers() {
        return iamService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('platform_owner')")
    public Map<String, Object> getUserDetail(@PathVariable Long id) {
        return iamService.getUserDetail(id);
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority('platform_owner')")
    public void updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        iamService.updateUserStatus(id, body.get("status"));
    }

    @PostMapping("/users/{id}/roles")
    @PreAuthorize("hasAuthority('platform_owner')")
    public void addRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        iamService.addRole(id, body.get("role"));
    }

    @DeleteMapping("/users/{id}/roles/{role}")
    @PreAuthorize("hasAuthority('platform_owner')")
    public void removeRole(@PathVariable Long id, @PathVariable String role) {
        iamService.removeRole(id, role);
    }
}
