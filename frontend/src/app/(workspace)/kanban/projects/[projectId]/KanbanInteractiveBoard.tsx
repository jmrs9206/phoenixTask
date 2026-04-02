"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { updateIssueStatus } from "../../../../../lib/api/workspace";
import type { KanbanBoard, KanbanIssueCard, KanbanSwimlane } from "../../../../../types/domain/workspace";
import AvatarBadge from "../../../../../components/ui/AvatarBadge";

type Props = {
  board: KanbanBoard;
  canMove: boolean;
};

type DragState = {
  issueId: number;
  fromStatus: string;
  laneId: string;
};

function cloneLanes(lanes: KanbanSwimlane[]) {
  return lanes.map((lane) => ({
    ...lane,
    columns: lane.columns.map((column) => ({
      ...column,
      issues: [...column.issues]
    }))
  }));
}

function recalcColumns(board: KanbanBoard, lanes: KanbanSwimlane[]) {
  const statusCount = new Map<string, number>();
  for (const lane of lanes) {
    for (const column of lane.columns) {
      statusCount.set(column.status, (statusCount.get(column.status) ?? 0) + column.issues.length);
    }
  }
  return board.columns.map((column) => {
    const totalCount = statusCount.get(column.status) ?? 0;
    return {
      ...column,
      totalCount,
      overLimit: column.wipLimit !== null && totalCount > column.wipLimit
    };
  });
}

export default function KanbanInteractiveBoard({ board, canMove }: Props) {
  const [boardState, setBoardState] = useState(board);
  const [dragging, setDragging] = useState<DragState | null>(null);
  const [dropStatus, setDropStatus] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  const orderedStatuses = useMemo(() => boardState.columns.map((column) => column.status), [boardState.columns]);
  const showLaneTitle = boardState.swimlanes.length > 1;

  const moveInBoard = (prev: KanbanBoard, drag: DragState, toStatus: string): KanbanBoard => {
    const lanes = cloneLanes(prev.swimlanes);
    const lane = lanes.find((item) => item.laneId === drag.laneId);
    if (!lane) {
      return prev;
    }
    const fromColumn = lane.columns.find((column) => column.status === drag.fromStatus);
    const toColumn = lane.columns.find((column) => column.status === toStatus);
    if (!fromColumn || !toColumn) {
      return prev;
    }
    const issueIndex = fromColumn.issues.findIndex((issue) => issue.issueId === drag.issueId);
    if (issueIndex < 0) {
      return prev;
    }
    const [issue] = fromColumn.issues.splice(issueIndex, 1);
    toColumn.issues.unshift(issue);
    return {
      ...prev,
      swimlanes: lanes,
      columns: recalcColumns(prev, lanes)
    };
  };

  const parseDragData = (raw: string | null): DragState | null => {
    if (!raw) {
      return null;
    }
    try {
      const parsed = JSON.parse(raw) as DragState;
      if (!parsed.issueId || !parsed.fromStatus || !parsed.laneId) {
        return null;
      }
      return parsed;
    } catch {
      return null;
    }
  };

  const handleDrop = async (laneId: string, targetStatus: string, payload: DragState | null) => {
    const activeDrag = payload ?? dragging;
    if (!activeDrag || isSaving) {
      return;
    }
    if (!canMove) {
      setFeedback("No tienes permisos para mover issues en Kanban.");
      setDragging(null);
      setDropStatus(null);
      return;
    }
    if (activeDrag.laneId !== laneId || activeDrag.fromStatus === targetStatus) {
      setDragging(null);
      setDropStatus(null);
      return;
    }

    const previousBoard = boardState;
    const nextBoard = moveInBoard(previousBoard, activeDrag, targetStatus);
    setBoardState(nextBoard);
    setFeedback(null);
    setDragging(null);
    setDropStatus(null);
    setIsSaving(true);
    try {
      await updateIssueStatus(activeDrag.issueId, targetStatus);
    } catch {
      setBoardState(previousBoard);
      setFeedback("No se pudo guardar el movimiento. Se restauró el estado anterior.");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <>
      {feedback ? <p className="form-message error">{feedback}</p> : null}
      <div className="kanban-board premium-kanban-board">
        <div className="kanban-columns-header">
          {boardState.columns.map((column) => (
            <div
              key={column.status}
              className={`kanban-column-header premium-kanban-column-header ${column.overLimit ? "is-over-limit" : ""}`}
            >
              <div className="kanban-column-title">
                <span>{column.title}</span>
                <span className="kanban-count">{column.totalCount}</span>
              </div>
              <div className="kanban-wip">
                WIP {column.totalCount}/{column.wipLimit ?? "—"}
              </div>
              <div className="kanban-policy">{column.policy}</div>
            </div>
          ))}
        </div>

        <div className="kanban-swimlanes">
          {boardState.swimlanes.map((lane) => (
            <div key={lane.laneId} className="kanban-lane">
              {showLaneTitle ? <div className="kanban-lane-title">{lane.label}</div> : null}
              <div className="kanban-lane-columns">
                {orderedStatuses.map((status) => {
                  const column = lane.columns.find((item) => item.status === status);
                  const issues = column?.issues ?? [];
                  const isDropTarget = dropStatus === status && dragging?.laneId === lane.laneId;

                  return (
                    <div
                      key={`${lane.laneId}-${status}`}
                      className={`kanban-column premium-kanban-column ${isDropTarget ? "is-drop-target" : ""}`}
                      onDragOver={(event) => {
                        if (!canMove || !dragging || dragging.laneId !== lane.laneId) {
                          return;
                        }
                        event.preventDefault();
                        event.dataTransfer.dropEffect = "move";
                        setDropStatus(status);
                      }}
                      onDragLeave={() => {
                        if (dropStatus === status) {
                          setDropStatus(null);
                        }
                      }}
                      onDrop={(event) => {
                        event.preventDefault();
                        const payload = parseDragData(event.dataTransfer.getData("application/x-phoenixtask-kanban"));
                        void handleDrop(lane.laneId, status, payload);
                      }}
                    >
                      <div className="kanban-cards">
                        {issues.map((issue: KanbanIssueCard) => {
                          const isDragging = dragging?.issueId === issue.issueId;
                          return (
                            <article
                              key={issue.issueId}
                              draggable={canMove && !isSaving}
                              onDragStart={(event) => {
                                const payload = { issueId: issue.issueId, fromStatus: status, laneId: lane.laneId };
                                event.dataTransfer.effectAllowed = "move";
                                event.dataTransfer.setData("application/x-phoenixtask-kanban", JSON.stringify(payload));
                                setDragging(payload);
                              }}
                              onDragEnd={() => {
                                setDragging(null);
                                setDropStatus(null);
                              }}
                              className={`kanban-card premium-kanban-card ${isDragging ? "is-dragging" : ""} ${canMove ? "is-draggable" : ""}`}
                            >
                              <div className="kanban-card-header">
                                <div className="kanban-card-title">
                                  <strong>{issue.issueKey}</strong>
                                  <span className="kanban-project-tag">{issue.projectKey}</span>
                                </div>
                                <span className={`kanban-priority priority-${issue.priority.toLowerCase()}`}>{issue.priority}</span>
                              </div>
                              <p>{issue.title}</p>
                              <div className="kanban-card-footer">
                                <div className="kanban-card-meta">
                                  <AvatarBadge name={issue.assigneeName} />
                                  <span className="muted">{issue.assigneeName}</span>
                                </div>
                                <span className="kanban-card-age">{issue.ageDays}d</span>
                              </div>
                              <Link href={`/issues/${issue.issueId}`} className="kanban-card-link">
                                Open issue
                              </Link>
                            </article>
                          );
                        })}
                        {issues.length === 0 ? <div className="kanban-empty-drop">Drop issue here</div> : null}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}
