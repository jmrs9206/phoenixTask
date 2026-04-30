package com.phoenixtask.iam;

import com.phoenixtask.iam.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.List;
import com.phoenixtask.iam.model.UserDetailResponse;

@RestController
@RequestMapping("/api/iam")
@Validated
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
    public UserDetailResponse getUserDetail(@PathVariable @Min(1) Long id) {
        return iamService.getUserDetail(id);
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority('platform_owner')")
    public void updateStatus(@PathVariable @Min(1) Long id, @Valid @RequestBody IamRequests.UpdateStatusRequest request) {
        iamService.updateUserStatus(id, request.status());
    }

    @PostMapping("/users/{id}/roles")
    @PreAuthorize("hasAuthority('platform_owner')")
    public void addRole(@PathVariable @Min(1) Long id, @Valid @RequestBody IamRequests.AddRoleRequest request) {
        iamService.addRole(id, request.role());
    }

    @DeleteMapping("/users/{id}/roles/{role}")
    @PreAuthorize("hasAuthority('platform_owner')")
    public void removeRole(@PathVariable @Min(1) Long id, @PathVariable String role) {
        iamService.removeRole(id, role);
    }
}
