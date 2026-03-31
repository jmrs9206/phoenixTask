"use client";

import { useMemo, useState, useTransition, type FormEvent } from "react";
import { useAuth } from "../../../../../components/auth/AuthProvider";
import { createGanttDependency } from "../../../../../lib/api/workspace";
import type { GanttIssue } from "../../../../../types/domain/workspace";

type Props = {
  projectId: number;
  issues: GanttIssue[];
};

export default function GanttDependencyForm({ projectId, issues }: Props) {
  const { can } = useAuth();
  const canUpdate = can("gantt.update");
  const [predecessorId, setPredecessorId] = useState<string>("");
  const [successorId, setSuccessorId] = useState<string>("");
  const [error, setError] = useState<string | null>(null);
  const [isPending, startTransition] = useTransition();

  const issueOptions = useMemo(
    () => issues.map((issue) => ({
      value: String(issue.issueId),
      label: `${issue.issueKey} · ${issue.title}`
    })),
    [issues]
  );

  if (!canUpdate) {
    return <p className="muted">You do not have permission to manage dependencies.</p>;
  }

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    if (!predecessorId || !successorId) {
      setError("Select both predecessor and successor issues.");
      return;
    }
    if (predecessorId === successorId) {
      setError("Dependency cannot reference the same issue.");
      return;
    }
    startTransition(async () => {
      try {
        await createGanttDependency(projectId, {
          predecessorIssueId: Number(predecessorId),
          successorIssueId: Number(successorId),
          dependencyType: "FS"
        });
        window.location.reload();
      } catch (err) {
        setError(err instanceof Error ? err.message : "Unable to create dependency");
      }
    });
  };

  return (
    <form className="list-filters compact" onSubmit={handleSubmit}>
      <div className="filter-field">
        <label htmlFor="dependency-predecessor">Predecessor (finish)</label>
        <select
          id="dependency-predecessor"
          value={predecessorId}
          onChange={(event) => setPredecessorId(event.target.value)}
        >
          <option value="">Select issue</option>
          {issueOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
      <div className="filter-field">
        <label htmlFor="dependency-successor">Successor (start)</label>
        <select
          id="dependency-successor"
          value={successorId}
          onChange={(event) => setSuccessorId(event.target.value)}
        >
          <option value="">Select issue</option>
          {issueOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>
      <button className="button-secondary" type="submit" disabled={isPending}>
        Add dependency
      </button>
      {error ? <span className="form-error">{error}</span> : null}
    </form>
  );
}
