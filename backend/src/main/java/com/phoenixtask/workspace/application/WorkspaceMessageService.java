package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.error.ConflictException;
import com.phoenixtask.shared.error.ForbiddenException;
import com.phoenixtask.shared.error.ResourceNotFoundException;
import com.phoenixtask.shared.error.ValidationException;
import com.phoenixtask.workspace.application.dto.DirectThreadRequest;
import com.phoenixtask.workspace.application.dto.DirectThreadResponse;
import com.phoenixtask.workspace.application.dto.MessageCreateRequest;
import com.phoenixtask.workspace.application.dto.MessageItemResponse;
import com.phoenixtask.workspace.application.dto.MessageThreadDirectResponse;
import com.phoenixtask.workspace.application.dto.MessageThreadProjectResponse;
import com.phoenixtask.workspace.application.dto.MessageThreadTeamResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceMemberRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceMessageRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceMessageThreadRepository;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceUserRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceMessageService {

  private final WorkspaceMessageThreadRepository threadRepository;
  private final WorkspaceMessageRepository messageRepository;
  private final WorkspaceMemberRepository memberRepository;
  private final WorkspaceUserRepository userRepository;

  public WorkspaceMessageService(
      WorkspaceMessageThreadRepository threadRepository,
      WorkspaceMessageRepository messageRepository,
      WorkspaceMemberRepository memberRepository,
      WorkspaceUserRepository userRepository
  ) {
    this.threadRepository = threadRepository;
    this.messageRepository = messageRepository;
    this.memberRepository = memberRepository;
    this.userRepository = userRepository;
  }

  public List<MessageThreadTeamResponse> listTeamThreads(Long currentUserId) {
    requireUser(currentUserId);
    if (threadRepository.teamThreadMissingForUser(currentUserId)) {
      throw new ConflictException("Team message thread missing");
    }
    return threadRepository.findTeamThreads(currentUserId).stream()
        .map(row -> new MessageThreadTeamResponse(
            row.threadId(),
            "TEAM",
            row.teamId(),
            row.teamName(),
            row.lastMessageAt()
        ))
        .collect(Collectors.toList());
  }

  public List<MessageThreadProjectResponse> listProjectThreads(Long currentUserId) {
    requireUser(currentUserId);
    if (threadRepository.projectThreadMissingForUser(currentUserId)) {
      throw new ConflictException("Project message thread missing");
    }
    return threadRepository.findProjectThreads(currentUserId).stream()
        .map(row -> new MessageThreadProjectResponse(
            row.threadId(),
            "PROJECT",
            row.projectId(),
            row.projectKey(),
            row.projectName(),
            row.lastMessageAt()
        ))
        .collect(Collectors.toList());
  }

  public List<MessageThreadDirectResponse> listDirectThreads(Long currentUserId) {
    requireUser(currentUserId);
    return threadRepository.findDirectThreads(currentUserId).stream()
        .map(row -> new MessageThreadDirectResponse(
            row.threadId(),
            "DIRECT",
            row.otherUserId(),
            row.otherUserFullName(),
            row.otherUserEmail(),
            row.lastMessageAt()
        ))
        .collect(Collectors.toList());
  }

  public List<MessageItemResponse> listMessages(Long currentUserId, Long threadId) {
    WorkspaceUserRepository.UserSummaryRow user = requireUser(currentUserId);
    WorkspaceMessageThreadRepository.ThreadRow thread = requireThread(threadId);
    ensureThreadAccess(user, thread);
    return messageRepository.findMessages(threadId).stream()
        .map(row -> new MessageItemResponse(
            row.messageId(),
            row.authorUserId(),
            row.authorFullName(),
            row.body(),
            row.createdAt()
        ))
        .collect(Collectors.toList());
  }

  @Transactional
  public MessageItemResponse sendMessage(Long currentUserId, Long threadId, MessageCreateRequest request) {
    WorkspaceUserRepository.UserSummaryRow user = requireUser(currentUserId);
    WorkspaceMessageThreadRepository.ThreadRow thread = requireThread(threadId);
    ensureThreadAccess(user, thread);

    String body = normalizeBody(request != null ? request.body() : null);
    WorkspaceMessageRepository.MessageInsertRow result =
        messageRepository.insertMessage(threadId, currentUserId, body);
    if (result == null) {
      throw new ConflictException("Failed to create message");
    }
    return new MessageItemResponse(
        result.messageId(),
        currentUserId,
        fullName(user),
        body,
        result.createdAt()
    );
  }

  @Transactional
  public DirectThreadCreationResult getOrCreateDirectThread(Long currentUserId, DirectThreadRequest request) {
    WorkspaceUserRepository.UserSummaryRow currentUser = requireUser(currentUserId);
    if (request == null || request.userId() == null) {
      throw new ValidationException("User id is required");
    }
    if (request.userId() <= 0) {
      throw new ValidationException("User id must be a positive number");
    }
    if (Objects.equals(currentUserId, request.userId())) {
      throw new ValidationException("Direct messages cannot be created with the same user");
    }

    WorkspaceUserRepository.UserSummaryRow otherUser = userRepository.findSummaryById(request.userId());
    if (otherUser == null) {
      throw new ResourceNotFoundException("User not found");
    }
    if (!Objects.equals(currentUser.companyId(), otherUser.companyId())) {
      throw new ValidationException("Users must belong to the same company");
    }

    long userOne = Math.min(currentUserId, request.userId());
    long userTwo = Math.max(currentUserId, request.userId());

    WorkspaceMessageThreadRepository.DirectThreadResultRow row =
        threadRepository.getOrCreateDirectThread(currentUser.companyId(), userOne, userTwo);
    if (row == null) {
      throw new ConflictException("Failed to create direct thread");
    }

    DirectThreadResponse response = new DirectThreadResponse(
        row.threadId(),
        "DIRECT",
        row.directUserOneId(),
        row.directUserTwoId(),
        row.lastMessageAt()
    );
    return new DirectThreadCreationResult(response, row.created());
  }

  private WorkspaceUserRepository.UserSummaryRow requireUser(Long userId) {
    if (userId == null) {
      throw new ValidationException("User id is required");
    }
    WorkspaceUserRepository.UserSummaryRow user = userRepository.findSummaryById(userId);
    if (user == null) {
      throw new ResourceNotFoundException("User not found");
    }
    return user;
  }

  private WorkspaceMessageThreadRepository.ThreadRow requireThread(Long threadId) {
    WorkspaceMessageThreadRepository.ThreadRow thread = threadRepository.findThreadById(threadId);
    if (thread == null) {
      throw new ResourceNotFoundException("Thread not found");
    }
    return thread;
  }

  private void ensureThreadAccess(
      WorkspaceUserRepository.UserSummaryRow user,
      WorkspaceMessageThreadRepository.ThreadRow thread
  ) {
    if (!Objects.equals(user.companyId(), thread.companyId())) {
      throw new ForbiddenException("Access to thread denied");
    }
    switch (thread.threadType()) {
      case "TEAM" -> {
        if (!memberRepository.teamMembershipExists(thread.teamId(), user.id())) {
          throw new ForbiddenException("Access to team messages denied");
        }
      }
      case "PROJECT" -> {
        if (!memberRepository.projectMembershipExists(thread.projectId(), user.id())) {
          throw new ForbiddenException("Access to project messages denied");
        }
      }
      case "DIRECT" -> {
        if (!Objects.equals(thread.directUserOneId(), user.id())
            && !Objects.equals(thread.directUserTwoId(), user.id())) {
          throw new ForbiddenException("Access to direct messages denied");
        }
      }
      default -> throw new ValidationException("Unsupported thread type");
    }
  }

  private String normalizeBody(String body) {
    if (body == null) {
      throw new ValidationException("Message body is required");
    }
    String trimmed = body.trim();
    if (trimmed.isEmpty()) {
      throw new ValidationException("Message body is required");
    }
    if (trimmed.length() > 2000) {
      throw new ValidationException("Message body exceeds 2000 characters");
    }
    return trimmed;
  }

  private String fullName(WorkspaceUserRepository.UserSummaryRow user) {
    return user.firstName() + " " + user.lastName();
  }

  public record DirectThreadCreationResult(
      DirectThreadResponse response,
      boolean created
  ) {}
}
