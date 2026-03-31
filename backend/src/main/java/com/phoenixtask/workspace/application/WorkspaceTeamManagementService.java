package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.error.ConflictException;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.dto.MemberAddRequest;
import com.phoenixtask.workspace.application.dto.MemberResponse;
import com.phoenixtask.workspace.application.dto.TeamCreateRequest;
import com.phoenixtask.workspace.application.dto.TeamResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceMessageThreadRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceMemberRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceTeamRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceUserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceTeamManagementService {

  private final WorkspaceTeamRepository teamRepository;
  private final WorkspaceMemberRepository memberRepository;
  private final WorkspaceUserRepository userRepository;
  private final WorkspaceMessageThreadRepository messageThreadRepository;

  public WorkspaceTeamManagementService(
      WorkspaceTeamRepository teamRepository,
      WorkspaceMemberRepository memberRepository,
      WorkspaceUserRepository userRepository,
      WorkspaceMessageThreadRepository messageThreadRepository
  ) {
    this.teamRepository = teamRepository;
    this.memberRepository = memberRepository;
    this.userRepository = userRepository;
    this.messageThreadRepository = messageThreadRepository;
  }

  @Transactional
  public TeamResponse createTeam(TeamCreateRequest request) {
    String name = request.name().trim();
    if (name.isBlank()) {
      throw new ValidationException("Team name is required");
    }
    if (teamRepository.existsByName(name)) {
      throw new ConflictException("Team name already exists");
    }

    Long companyId = teamRepository.getCompanyId();
    if (companyId == null) {
      throw new ResourceNotFoundException("Company not found");
    }
    String description = normalizeDescription(request.description());
    Long id = teamRepository.insertTeam(companyId, name, description);
    Long threadId = messageThreadRepository.createTeamThread(companyId, id);
    if (threadId == null) {
      throw new ConflictException("Team message thread missing");
    }
    LocalDateTime now = teamRepository.fetchCreatedAt(id);
    return new TeamResponse(id, companyId, name, description, now, now);
  }

  public List<MemberResponse> listMembers(Long teamId) {
    ensureTeamExists(teamId);
    return memberRepository.findTeamMembers(teamId).stream()
        .map(this::toMemberResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public void addMember(Long teamId, MemberAddRequest request) {
    ensureTeamExists(teamId);
    ensureUserExists(request.userId());
    Boolean assignable = memberRepository.getRoleAssignability(request.roleId());
    if (assignable == null) {
      throw new ResourceNotFoundException("Role not found");
    }
    if (!assignable) {
      throw new ValidationException("Role is not assignable");
    }
    if (memberRepository.teamMembershipExists(teamId, request.userId())) {
      throw new ConflictException("User already belongs to team");
    }
    memberRepository.insertTeamMembership(teamId, request.userId(), request.roleId());
  }

  private void ensureTeamExists(Long teamId) {
    if (!teamRepository.existsById(teamId)) {
      throw new ResourceNotFoundException("Team not found");
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
