package com.phoenixtask.kanban.model;

import java.util.List;

public class KanbanColumn {
    private String status;
    private List<KanbanIssueCard> issues;

    public KanbanColumn() {}

    public KanbanColumn(String status, List<KanbanIssueCard> issues) {
        this.status = status;
        this.issues = issues;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<KanbanIssueCard> getIssues() { return issues; }
    public void setIssues(List<KanbanIssueCard> issues) { this.issues = issues; }
}
