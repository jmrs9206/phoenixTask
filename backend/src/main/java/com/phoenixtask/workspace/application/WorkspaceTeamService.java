package com.phoenixtask.workspace.application;

import com.phoenixtask.shared.interfaces.PageRequest;
import com.phoenixtask.shared.interfaces.PageResponse;
import com.phoenixtask.workspace.application.dto.TeamListQuery;
import com.phoenixtask.workspace.application.dto.TeamResponse;
import com.phoenixtask.workspace.infrastructure.persistence.WorkspaceTeamRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class WorkspaceTeamService {

  private final WorkspaceTeamRepository repository;

  public WorkspaceTeamService(WorkspaceTeamRepository repository) {
    this.repository = repository;
  }

  public PageResponse<TeamResponse> listTeams(PageRequest pageRequest, TeamListQuery query) {
    long total = repository.countAll(query.query());
    List<TeamResponse> items = repository.findAllPaged(
            query.query(),
            pageRequest.pageSize(),
            pageRequest.offset()
        ).stream()
        .map(row -> new TeamResponse(
            row.id(),
            row.companyId(),
            row.name(),
            row.description(),
            row.createdAt(),
            row.updatedAt()
        ))
        .collect(Collectors.toList());
    return PageResponse.of(items, pageRequest, total);
  }
}
