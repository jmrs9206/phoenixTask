"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { createOkrCheckin } from "../../../lib/api/workspace";
import { useAuth } from "../../../components/auth/AuthProvider";

type ObjectiveCheckinFormProps = {
  objectiveId: number;
  disabled?: boolean;
};

export default function ObjectiveCheckinForm({ objectiveId, disabled }: ObjectiveCheckinFormProps) {
  const { can } = useAuth();
  const canUpdate = can("okr.objective.update");
  const isDisabled = disabled || !canUpdate;
  const router = useRouter();
  const [progressPercent, setProgressPercent] = useState<string>("");
  const [confidenceLevel, setConfidenceLevel] = useState<"LOW" | "MEDIUM" | "HIGH">("MEDIUM");
  const [note, setNote] = useState("");
  const [state, setState] = useState<"idle" | "submitting" | "success" | "error">("idle");
  const [message, setMessage] = useState<string | null>(null);
  const feedbackMessage = isDisabled ? "You do not have permission to add check-ins." : message;
  const feedbackState = isDisabled ? "error" : state;

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (isDisabled) {
      return;
    }
    setState("submitting");
    setMessage(null);
    try {
      const progressValue = progressPercent.trim() === "" ? null : Number(progressPercent);
      if (progressValue !== null && (!Number.isFinite(progressValue) || progressValue < 0 || progressValue > 100)) {
        throw new Error("Progress must be between 0 and 100");
      }
      await createOkrCheckin(objectiveId, {
        progressPercent: progressValue,
        confidenceLevel,
        note: note.trim() || null
      });
      setState("success");
      setMessage("Check-in saved.");
      setProgressPercent("");
      setConfidenceLevel("MEDIUM");
      setNote("");
      router.refresh();
    } catch (error) {
      setState("error");
      setMessage("Please correct the highlighted fields.");
    }
  };

  return (
    <form className="form form-columns" onSubmit={handleSubmit}>
      <label>
        Progress % (optional)
        <input
          type="number"
          min="0"
          max="100"
          step="0.1"
          value={progressPercent}
          onChange={(event) => setProgressPercent(event.target.value)}
          disabled={isDisabled}
        />
      </label>
      <label>
        Confidence
        <select
          value={confidenceLevel}
          onChange={(event) => setConfidenceLevel(event.target.value as "LOW" | "MEDIUM" | "HIGH")}
          disabled={isDisabled}
        >
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
        </select>
      </label>
      <label>
        Update
        <input
          value={note}
          onChange={(event) => setNote(event.target.value)}
          maxLength={500}
          disabled={isDisabled}
          placeholder="Share progress, blockers, or decisions"
        />
      </label>
      <div className="form-actions">
        <button type="submit" disabled={isDisabled || state === "submitting"}>
          {state === "submitting" ? "Saving..." : "Add Check-in"}
        </button>
        {feedbackMessage && <span className={`form-message ${feedbackState}`}>{feedbackMessage}</span>}
      </div>
    </form>
  );
}
