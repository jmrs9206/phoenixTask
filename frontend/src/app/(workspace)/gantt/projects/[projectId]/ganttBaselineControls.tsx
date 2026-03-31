"use client";

import { useState, useTransition } from "react";
import { useAuth } from "../../../../../components/auth/AuthProvider";
import { captureGanttBaseline } from "../../../../../lib/api/workspace";
import type { GanttBaseline } from "../../../../../types/domain/workspace";

type Props = {
  projectId: number;
  baseline: GanttBaseline | null;
};

export default function GanttBaselineControls({ projectId, baseline }: Props) {
  const { can } = useAuth();
  const canUpdate = can("gantt.update");
  const [error, setError] = useState<string | null>(null);
  const [isPending, startTransition] = useTransition();

  if (!canUpdate) {
    return null;
  }

  const handleCapture = () => {
    setError(null);
    startTransition(async () => {
      try {
        await captureGanttBaseline(projectId);
        window.location.reload();
      } catch (err) {
        setError(err instanceof Error ? err.message : "Unable to capture baseline");
      }
    });
  };

  return (
    <div className="inline-actions">
      <button className="button-secondary" type="button" onClick={handleCapture} disabled={isPending}>
        {baseline ? "Update baseline" : "Capture baseline"}
      </button>
      {error ? <span className="form-error">{error}</span> : null}
    </div>
  );
}
