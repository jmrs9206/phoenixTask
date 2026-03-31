"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { updateSprint } from "../../../../../lib/api/workspace";
import { useAuth } from "../../../../../components/auth/AuthProvider";
import type { Sprint, SprintHealth } from "../../../../../types/domain/workspace";

type SprintHealthPanelProps = {
  sprint: Sprint;
  health: SprintHealth | null;
};

export default function SprintHealthPanel({ sprint, health }: SprintHealthPanelProps) {
  const { can } = useAuth();
  const canUpdate = can("scrum.sprint.update");
  const router = useRouter();
  const [goal, setGoal] = useState(sprint.goal ?? "");
  const [status, setStatus] = useState(sprint.status);
  const [state, setState] = useState<"idle" | "saving" | "success" | "error">("idle");
  const [message, setMessage] = useState<string | null>(null);

  const sayDoLabel = useMemo(() => {
    if (!health?.sayDoPercent && health?.sayDoPercent !== 0) {
      return "—";
    }
    return `${health.sayDoPercent.toFixed(1)}%`;
  }, [health?.sayDoPercent]);

  const handleSave = async () => {
    if (!canUpdate) {
      return;
    }
    setState("saving");
    setMessage(null);
    try {
      await updateSprint(sprint.id, {
        goal: goal.trim() ? goal.trim() : null,
        status
      });
      setState("success");
      setMessage("Sprint updated.");
      router.refresh();
    } catch {
      setState("error");
      setMessage("Unable to update sprint.");
    }
  };

  return (
    <div className="sprint-health">
      <div className="sprint-health-header">
        <div>
          <h3 className="section-title">Sprint Health</h3>
          <p className="description-text">
            Active sprint: <strong>{sprint.name}</strong> ({sprint.startDate} → {sprint.endDate})
          </p>
        </div>
        <span className={`health-pill health-pill--${(health?.healthStatus || "unknown").toLowerCase()}`}>
          {health?.healthStatus ?? "NO_DATA"}
        </span>
      </div>

      <div className="sprint-health-grid">
        <div className="health-card">
          <label>Goal</label>
          <textarea
            value={goal}
            onChange={(event) => setGoal(event.target.value)}
            placeholder="Define the sprint goal"
            maxLength={255}
            disabled={!canUpdate}
          />
          <div className="health-actions">
            <select value={status} onChange={(event) => setStatus(event.target.value)} disabled={!canUpdate}>
              <option value="PLANNED">PLANNED</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="COMPLETED">COMPLETED</option>
            </select>
            <button className="button-secondary" onClick={handleSave} disabled={!canUpdate || state === "saving"}>
              {state === "saving" ? "Saving..." : "Save sprint"}
            </button>
          </div>
          {message && <span className={`form-message ${state}`}>{message}</span>}
          {!canUpdate && <span className="muted">You do not have permission to update sprint details.</span>}
        </div>

        <div className="health-card">
          <label>Commitment snapshot</label>
          {health?.commitment ? (
            <div className="health-metrics">
              <div>
                <strong>{health.commitment.committedCount}</strong>
                <span>Committed issues</span>
              </div>
              <div>
                <strong>{health.commitment.completedCount}</strong>
                <span>Completed</span>
              </div>
              <div>
                <strong>{health.commitment.remainingCount}</strong>
                <span>Remaining</span>
              </div>
              <div>
                <strong>{sayDoLabel}</strong>
                <span>Say / Do</span>
              </div>
              <div className="health-meta">
                Snapshot: {new Date(health.commitment.capturedAt).toLocaleString()}
              </div>
              <div className="health-meta">
                Say / Do = completed ÷ committed from the captured snapshot.
              </div>
            </div>
          ) : (
            <p className="muted">Commitment snapshot will appear when the sprint is activated.</p>
          )}
        </div>

        <div className="health-card">
          <label>Burndown</label>
          {health?.burndown?.length ? (
            <div className="burndown-list">
              {health.burndown.map((point) => {
                const ratio =
                  point.committedCount === 0 ? 0 : Math.round((point.remainingCount / point.committedCount) * 100);
                return (
                  <div key={point.date} className="burndown-row">
                    <div className="burndown-meta">
                      <span>{point.date}</span>
                      <span>
                        {point.remainingCount}/{point.committedCount} remaining
                      </span>
                    </div>
                    <div className="burndown-bar">
                      <div className="burndown-bar-fill" style={{ width: `${ratio}%` }} />
                    </div>
                  </div>
                );
              })}
              <div className="health-meta">
                Burndown points are derived from the captured commitment snapshot.
              </div>
            </div>
          ) : (
            <p className="muted">Burndown points will appear once the sprint commitment is captured.</p>
          )}
        </div>
      </div>
    </div>
  );
}
