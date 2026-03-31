"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import EmptyState from "../../../../../components/ui/EmptyState";
import { assignIssueToSprint } from "../../../../../lib/api/workspace";
import type { IssueSummary, Sprint } from "../../../../../types/domain/workspace";
import { useAuth } from "../../../../../components/auth/AuthProvider";

type SprintAssignFormProps = {
  projectId: number;
  backlog: IssueSummary[];
  sprints: Sprint[];
};

export default function SprintAssignForm({ backlog, sprints }: SprintAssignFormProps) {
  const { can } = useAuth();
  const canAssign = can("scrum.backlog.update");
  const router = useRouter();
  const availableSprints = useMemo(
    () => sprints.filter((sprint) => sprint.status !== "COMPLETED"),
    [sprints]
  );
  const [issueId, setIssueId] = useState<number | "">("");
  const [sprintId, setSprintId] = useState<number | "">("");
  const [state, setState] = useState<"idle" | "submitting" | "success" | "error">("idle");
  const [message, setMessage] = useState<string | null>(null);
  const feedbackMessage = canAssign ? message : "You do not have permission to perform this action.";
  const feedbackState = canAssign ? state : "error";

  const handleAssign = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!canAssign) {
      return;
    }
    setState("submitting");
    setMessage(null);
    try {
      if (!issueId || !sprintId) {
        throw new Error("Select an issue and a sprint");
      }
      await assignIssueToSprint(Number(sprintId), { issueId: Number(issueId) });
      setState("success");
      setMessage("Saved successfully.");
      setIssueId("");
      setSprintId("");
      router.refresh();
    } catch (error) {
      setState("error");
      setMessage("Please correct the highlighted fields.");
    }
  };

  if (backlog.length === 0 || availableSprints.length === 0) {
    return <EmptyState size="compact" />;
  }

  return (
    <form className="form inline-form" onSubmit={handleAssign}>
      <label>
        Backlog issue
        <select
          value={issueId}
          onChange={(event) => setIssueId(Number(event.target.value) || "")}
          disabled={!canAssign}
        >
          <option value="">Select issue</option>
          {backlog.map((issue) => (
            <option key={issue.id} value={issue.id}>
              {issue.issueKey} · {issue.title}
            </option>
          ))}
        </select>
      </label>
      <label>
        Sprint
        <select
          value={sprintId}
          onChange={(event) => setSprintId(Number(event.target.value) || "")}
          disabled={!canAssign}
        >
          <option value="">Select sprint</option>
          {availableSprints.map((sprint) => (
            <option key={sprint.id} value={sprint.id}>
              {sprint.name} ({sprint.status})
            </option>
          ))}
        </select>
      </label>
      <div className="form-actions">
        <button type="submit" disabled={!canAssign || state === "submitting"}>
          {state === "submitting" ? "Assigning..." : "Assign Issue"}
        </button>
        {feedbackMessage && <span className={`form-message ${feedbackState}`}>{feedbackMessage}</span>}
      </div>
    </form>
  );
}
