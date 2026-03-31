"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import EmptyState from "../../../../../components/ui/EmptyState";
import { moveIssueToBacklog } from "../../../../../lib/api/workspace";
import type { IssueSummary } from "../../../../../types/domain/workspace";
import { useAuth } from "../../../../../components/auth/AuthProvider";

type BacklogMoveFormProps = {
  sprintIssues: IssueSummary[];
};

export default function BacklogMoveForm({ sprintIssues }: BacklogMoveFormProps) {
  const { can } = useAuth();
  const canMove = can("scrum.backlog.update");
  const router = useRouter();
  const [issueId, setIssueId] = useState<number | "">("");
  const [state, setState] = useState<"idle" | "submitting" | "success" | "error">("idle");
  const [message, setMessage] = useState<string | null>(null);
  const feedbackMessage = canMove ? message : "You do not have permission to perform this action.";
  const feedbackState = canMove ? state : "error";

  const handleMove = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!canMove) {
      return;
    }
    setState("submitting");
    setMessage(null);
    try {
      if (!issueId) {
        throw new Error("Select an issue to move");
      }
      await moveIssueToBacklog({ issueId: Number(issueId) });
      setState("success");
      setMessage("Saved successfully.");
      setIssueId("");
      router.refresh();
    } catch (error) {
      setState("error");
      setMessage("Please correct the highlighted fields.");
    }
  };

  if (sprintIssues.length === 0) {
    return <EmptyState size="compact" />;
  }

  return (
    <form className="form inline-form" onSubmit={handleMove}>
      <label>
        Sprint issue
        <select value={issueId} onChange={(event) => setIssueId(Number(event.target.value) || "")} disabled={!canMove}>
          <option value="">Select issue</option>
          {sprintIssues.map((issue) => (
            <option key={issue.id} value={issue.id}>
              {issue.issueKey} · {issue.title}
            </option>
          ))}
        </select>
      </label>
      <div className="form-actions">
        <button type="submit" disabled={!canMove || state === "submitting"}>
          {state === "submitting" ? "Moving..." : "Move to Backlog"}
        </button>
        {feedbackMessage && <span className={`form-message ${feedbackState}`}>{feedbackMessage}</span>}
      </div>
    </form>
  );
}
