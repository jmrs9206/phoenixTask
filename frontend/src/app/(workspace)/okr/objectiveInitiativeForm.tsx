"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { linkOkrInitiative } from "../../../lib/api/workspace";
import { useAuth } from "../../../components/auth/AuthProvider";
import type { IssueSummary } from "../../../types/domain/workspace";

type ObjectiveInitiativeFormProps = {
  objectiveId: number;
  issues: IssueSummary[];
  disabled?: boolean;
};

export default function ObjectiveInitiativeForm({ objectiveId, issues, disabled }: ObjectiveInitiativeFormProps) {
  const { can } = useAuth();
  const canUpdate = can("okr.objective.update");
  const canViewIssues = can("issues.view");
  const isDisabled = disabled || !canUpdate || !canViewIssues || issues.length === 0;
  const router = useRouter();
  const [issueId, setIssueId] = useState<number>(issues[0]?.id ?? 0);
  const [state, setState] = useState<"idle" | "submitting" | "success" | "error">("idle");
  const [message, setMessage] = useState<string | null>(null);

  const feedbackMessage = !canUpdate
    ? "You do not have permission to link initiatives."
    : !canViewIssues
      ? "Issue access is required to link initiatives."
      : issues.length === 0
        ? "No issues available to link."
        : message;
  const feedbackState = !canUpdate || !canViewIssues || issues.length === 0 ? "error" : state;

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (isDisabled) {
      return;
    }
    setState("submitting");
    setMessage(null);
    try {
      await linkOkrInitiative(objectiveId, { issueId });
      setState("success");
      setMessage("Initiative linked.");
      router.refresh();
    } catch (error) {
      setState("error");
      setMessage("Unable to link this initiative.");
    }
  };

  return (
    <form className="form form-columns" onSubmit={handleSubmit}>
      <label>
        Link issue
        <select
          value={issueId}
          onChange={(event) => setIssueId(Number(event.target.value))}
          disabled={isDisabled}
        >
          {issues.map((issue) => (
            <option key={issue.id} value={issue.id}>
              {issue.issueKey} · {issue.title}
            </option>
          ))}
        </select>
      </label>
      <div className="form-actions">
        <button type="submit" disabled={isDisabled || state === "submitting"}>
          {state === "submitting" ? "Linking..." : "Link Initiative"}
        </button>
        {feedbackMessage && <span className={`form-message ${feedbackState}`}>{feedbackMessage}</span>}
      </div>
    </form>
  );
}
