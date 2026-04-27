package com.phoenixtask.kanban.model;

import java.util.List;

public class KanbanBoard {
    private Long projectId;
    private Long sprintId;
    private List<KanbanColumn> columns;

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getSprintId() { return sprintId; }
    public void setSprintId(Long sprintId) { this.sprintId = sprintId; }
    public List<KanbanColumn> getColumns() { return columns; }
    public void setColumns(List<KanbanColumn> columns) { this.columns = columns; }
}
