package com.phoenixtask.kanban;

import com.phoenixtask.kanban.model.KanbanBoard;
import com.phoenixtask.kanban.model.KanbanColumn;
import com.phoenixtask.kanban.model.KanbanIssueCard;
import com.phoenixtask.kanban.repository.KanbanRepository;
import com.phoenixtask.issues.model.Issue;
import com.phoenixtask.issues.repository.IssueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KanbanService {
    private static final List<String> VALID_STATUSES = Arrays.asList("BACKLOG", "TODO", "IN_PROGRESS", "IN_REVIEW", "DONE");
    
    private final KanbanRepository kanbanRepository;
    private final IssueRepository issueRepository;

    public KanbanService(KanbanRepository kanbanRepository, IssueRepository issueRepository) {
        this.kanbanRepository = kanbanRepository;
        this.issueRepository = issueRepository;
    }

    public KanbanBoard getBoard(Long projectId, Long sprintId) {
        List<KanbanIssueCard> allCards = kanbanRepository.findCardsByProject(projectId, sprintId);
        Map<String, List<KanbanIssueCard>> grouped = allCards.stream()
                .collect(Collectors.groupingBy(KanbanIssueCard::getStatus));

        List<KanbanColumn> columns = VALID_STATUSES.stream()
                .map(status -> new KanbanColumn(status, grouped.getOrDefault(status, new ArrayList<>())))
                .collect(Collectors.toList());

        KanbanBoard board = new KanbanBoard();
        board.setProjectId(projectId);
        board.setSprintId(sprintId);
        board.setColumns(columns);
        return board;
    }

    @Transactional
    public KanbanIssueCard moveIssue(Long issueId, Long projectId, String targetStatus, int targetIndex) {
        if (!VALID_STATUSES.contains(targetStatus)) {
            throw new IllegalArgumentException("Invalid status: " + targetStatus);
        }

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        if (!issue.getProjectId().equals(projectId)) {
            throw new IllegalArgumentException("Issue does not belong to the specified project");
        }

        String sourceStatus = issue.getStatus();
        
        // 1. Reorder target column
        List<KanbanIssueCard> targetCards = kanbanRepository.findCardsByColumn(projectId, targetStatus);
        
        // Remove from target list if it was already there (same status move)
        targetCards.removeIf(c -> c.getId().equals(issueId));
        
        // Create the card for the moving issue
        KanbanIssueCard movingCard = new KanbanIssueCard();
        movingCard.setId(issue.getId());
        movingCard.setStatus(targetStatus);
        // ... other fields not strictly needed for reordering but good to have
        
        // Insert at targetIndex
        if (targetIndex < 0) targetIndex = 0;
        if (targetIndex >= targetCards.size()) {
            targetCards.add(movingCard);
        } else {
            targetCards.add(targetIndex, movingCard);
        }
        
        // Update all positions in target column
        for (int i = 0; i < targetCards.size(); i++) {
            kanbanRepository.updatePositionAndStatus(targetCards.get(i).getId(), targetStatus, (long) i);
        }

        // 2. If it was a different status, reorder source column to close gap
        if (!sourceStatus.equals(targetStatus)) {
            List<KanbanIssueCard> sourceCards = kanbanRepository.findCardsByColumn(projectId, sourceStatus);
            sourceCards.removeIf(c -> c.getId().equals(issueId));
            for (int i = 0; i < sourceCards.size(); i++) {
                kanbanRepository.updatePosition(sourceCards.get(i).getId(), (long) i);
            }
        }

        return kanbanRepository.findCardById(issueId).orElseThrow();
    }
}
