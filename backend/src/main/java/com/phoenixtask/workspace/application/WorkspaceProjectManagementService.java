package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.error.ConflictException;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.dto.MemberAddRequest;
import com.phoenixtask.workspace.application.dto.MemberResponse;
import com.phoenixtask.workspace.application.dto.ProjectCreateRequest;
import com.phoenixtask.workspace.application.dto.ProjectResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceMessageThreadRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceMemberRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceProjectRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceUserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceProjectManagementService {

  private final WorkspaceProjectRepository projectRepository;
  private final WorkspaceMemberRepository memberRepository;
  private final WorkspaceUserRepository userRepository;
  private final WorkspaceMessageThreadRepository messageThreadRepository;

  public WorkspaceProjectManagementService(
      WorkspaceProjectRepository projectRepository,
      WorkspaceMemberRepository memberRepository,
      WorkspaceUserRepository userRepository,
      WorkspaceMessageThreadRepository messageThreadRepository
  ) {
    this.projectRepository = projectRepository;
    this.memberRepository = memberRepository;
    this.userRepository = userRepository;
    this.messageThreadRepository = messageThreadRepository;
  }

  @Transactional
  public ProjectResponse createProject(ProjectCreateRequest request) {
    String key = request.projectKey().trim().toUpperCase();
    if (!key.matches("^[A-Z][A-Z0-9]{1,14}$")) {
      throw new ValidationException("Project key format is invalid");
    }
    if (projectRepository.existsByProjectKey(key)) {
      throw new ConflictException("Project key already exists");
    }
    String name = request.name().trim();
    if (name.isBlank()) {
      throw new ValidationException("Project name is required");
    }

    Long companyId = projectRepository.getCompanyId();
    if (companyId == null) {
      throw new ResourceNotFoundException("Company not found");
    }
    String description = normalizeDescription(request.description());
    Long id = projectRepository.insertProject(companyId, key, name, description);
    Long threadId = messageThreadRepository.createProjectThread(companyId, id);
    if (threadId == null) {
      throw new ConflictException("Project message thread missing");
    }
    LocalDateTime now = projectRepository.fetchCreatedAt(id);
    return new ProjectResponse(id, companyId, key, name, description, "ACTIVE", now, now);
  }

  public List<MemberResponse> listMembers(Long projectId) {
    ensureProjectExists(projectId);
    return memberRepository.findProjectMembers(projectId).stream()
        .map(this::toMemberResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public void addMember(Long projectId, MemberAddRequest request) {
    ensureProjectExists(projectId);
    ensureUserExists(request.userId());
    Boolean assignable = memberRepository.getRoleAssignability(request.roleId());
    if (assignable == null) {
      throw new ResourceNotFoundException("Role not found");
    }
    if (!assignable) {
      throw new ValidationException("Role is not assignable");
    }
    if (memberRepository.projectMembershipExists(projectId, request.userId())) {
      throw new ConflictException("User already belongs to project");
    }
    memberRepository.insertProjectMembership(projectId, request.userId(), request.roleId());
  }

  private void ensureProjectExists(Long projectId) {
    if (!projectRepository.existsById(projectId)) {
      throw new ResourceNotFoundException("Project not found");
    }
  }

  private void ensureUserExists(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User not found");
    }
  }

  private MemberResponse toMemberResponse(WorkspaceMemberRepository.MemberRow row) {
    return new MemberResponse(
        row.userId(),
        row.fullName(),
        row.email(),
        row.roleCode(),
        row.roleName(),
        row.status()
    );
  }

  private String normalizeDescription(String description) {
    if (description == null || description.isBlank()) {
      return null;
    }
    return description.trim();
  }
}
