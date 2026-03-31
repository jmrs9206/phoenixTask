"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { closeOkrObjective } from "../../../lib/api/workspace";
import { useAuth } from "../../../components/auth/AuthProvider";

type ObjectiveCloseFormProps = {
  objectiveId: number;
  status: string;
  finalScore: number | null;
  closedAt: string | null;
};

export default function ObjectiveCloseForm({ objectiveId, status, finalScore, closedAt }: ObjectiveCloseFormProps) {
  const { can } = useAuth();
  const canClose = can("okr.objective.close");
  const isClosed = status === "COMPLETED" || status === "CANCELLED";
  const router = useRouter();
  const [closeStatus, setCloseStatus] = useState<"COMPLETED" | "CANCELLED">("COMPLETED");
  const [score, setScore] = useState<string>(finalScore?.toString() ?? "0");
  const [state, setState] = useState<"idle" | "submitting" | "success" | "error">("idle");
  const [message, setMessage] = useState<string | null>(null);

  if (isClosed) {
    return (
      <div className="okr-status-box">
        <div>
          <strong>{status}</strong>
          {finalScore !== null && <span> · Final score: {finalScore.toFixed(1)}%</span>}
        </div>
        {closedAt && <span className="muted">Closed {new Date(closedAt).toLocaleDateString()}</span>}
      </div>
    );
  }

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!canClose) {
      return;
    }
    setState("submitting");
    setMessage(null);
    try {
      const scoreValue = Number(score);
      if (!Number.isFinite(scoreValue) || scoreValue < 0 || scoreValue > 100) {
        throw new Error("Score must be between 0 and 100");
      }
      await closeOkrObjective(objectiveId, {
        status: closeStatus,
        finalScore: scoreValue
      });
      setState("success");
      setMessage("Objective closed.");
      router.refresh();
    } catch (error) {
      setState("error");
      setMessage("Unable to close objective.");
    }
  };

  return (
    <form className="form form-columns" onSubmit={handleSubmit}>
      <label>
        Close status
        <select
          value={closeStatus}
          onChange={(event) => setCloseStatus(event.target.value as "COMPLETED" | "CANCELLED")}
          disabled={!canClose}
        >
          <option value="COMPLETED">COMPLETED</option>
          <option value="CANCELLED">CANCELLED</option>
        </select>
      </label>
      <label>
        Final score (0-100)
        <input
          type="number"
          min="0"
          max="100"
          step="0.1"
          value={score}
          onChange={(event) => setScore(event.target.value)}
          disabled={!canClose}
        />
      </label>
      <div className="form-actions">
        <button type="submit" disabled={!canClose || state === "submitting"}>
          {state === "submitting" ? "Closing..." : "Close Objective"}
        </button>
        {(message || !canClose) && (
          <span className={`form-message ${canClose ? state : "error"}`}>
            {canClose ? message : "You do not have permission to close objectives."}
          </span>
        )}
      </div>
    </form>
  );
}
