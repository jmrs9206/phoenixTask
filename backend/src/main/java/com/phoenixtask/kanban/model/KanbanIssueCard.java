package com.phoenixtask.kanban.model;

public class KanbanIssueCard {
    private Long id;
    private String issueKey;
    private String title;
    private String status;
    private String priority;
    private Long assigneeUserId;
    private Long sprintId;
    private Long kanbanPosition;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIssueKey() { return issueKey; }
    public void setIssueKey(String issueKey) { this.issueKey = issueKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Long getAssigneeUserId() { return assigneeUserId; }
    public void setAssigneeUserId(Long assigneeUserId) { this.assigneeUserId = assigneeUserId; }
    public Long getSprintId() { return sprintId; }
    public void setSprintId(Long sprintId) { this.sprintId = sprintId; }
    public Long getKanbanPosition() { return kanbanPosition; }
    public void setKanbanPosition(Long kanbanPosition) { this.kanbanPosition = kanbanPosition; }
}
