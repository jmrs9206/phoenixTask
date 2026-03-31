"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import EmptyState from "../../../components/ui/EmptyState";
import { createOkrKeyResult } from "../../../lib/api/workspace";
import type { OkrObjective } from "../../../types/domain/workspace";
import { useAuth } from "../../../components/auth/AuthProvider";

type KeyResultCreateFormProps = {
  objectives: OkrObjective[];
};

export default function KeyResultCreateForm({ objectives }: KeyResultCreateFormProps) {
  const { can } = useAuth();
  const canCreate = can("okr.key_result.manage");
  const router = useRouter();
  const [objectiveId, setObjectiveId] = useState<number>(objectives[0]?.id ?? 0);
  const [title, setTitle] = useState("");
  const [targetValue, setTargetValue] = useState("0");
  const [currentValue, setCurrentValue] = useState("0");
  const [unit, setUnit] = useState("percent");
  const [status, setStatus] = useState("ON_TRACK");
  const [state, setState] = useState<"idle" | "submitting" | "success" | "error">("idle");
  const [message, setMessage] = useState<string | null>(null);
  const feedbackMessage = canCreate ? message : "You do not have permission to perform this action.";
  const feedbackState = canCreate ? state : "error";

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!canCreate) {
      return;
    }
    setState("submitting");
    setMessage(null);
    try {
      if (!objectiveId) {
        throw new Error("Select an objective");
      }
      if (!title.trim()) {
        throw new Error("Key result title is required");
      }
      const targetNumber = Number(targetValue);
      const currentNumber = Number(currentValue);
      if (!Number.isFinite(targetNumber) || targetNumber <= 0) {
        throw new Error("Target value must be greater than zero");
      }
      if (!Number.isFinite(currentNumber) || currentNumber < 0) {
        throw new Error("Current value must be zero or higher");
      }
      await createOkrKeyResult(objectiveId, {
        title: title.trim(),
        targetValue: targetNumber,
        currentValue: currentNumber,
        unit: unit.trim(),
        status
      });
      setState("success");
      setMessage("Saved successfully.");
      setTitle("");
      setTargetValue("0");
      setCurrentValue("0");
      setUnit("percent");
      setStatus("ON_TRACK");
      router.refresh();
    } catch (error) {
      setState("error");
      setMessage("Please correct the highlighted fields.");
    }
  };

  if (objectives.length === 0) {
    return <EmptyState size="compact" />;
  }

  return (
    <form className="form form-columns" onSubmit={handleSubmit}>
      <label>
        Objective
        <select
          value={objectiveId}
          onChange={(event) => setObjectiveId(Number(event.target.value))}
          disabled={!canCreate}
        >
          {objectives.map((objective) => (
            <option key={objective.id} value={objective.id}>
              {objective.title}
            </option>
          ))}
        </select>
      </label>
      <label>
        Title
        <input
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          maxLength={200}
          required
          disabled={!canCreate}
        />
      </label>
      <label>
        Target value
        <input
          type="number"
          min="0"
          step="0.01"
          value={targetValue}
          onChange={(event) => setTargetValue(event.target.value)}
          disabled={!canCreate}
        />
      </label>
      <label>
        Current value
        <input
          type="number"
          min="0"
          step="0.01"
          value={currentValue}
          onChange={(event) => setCurrentValue(event.target.value)}
          disabled={!canCreate}
        />
      </label>
      <label>
        Unit
        <input value={unit} onChange={(event) => setUnit(event.target.value)} maxLength={32} disabled={!canCreate} />
      </label>
      <label>
        Status
        <select value={status} onChange={(event) => setStatus(event.target.value)} disabled={!canCreate}>
          <option value="ON_TRACK">ON_TRACK</option>
          <option value="AT_RISK">AT_RISK</option>
          <option value="OFF_TRACK">OFF_TRACK</option>
          <option value="COMPLETED">COMPLETED</option>
        </select>
      </label>
      <div className="form-actions">
        <button type="submit" disabled={!canCreate || state === "submitting"}>
          {state === "submitting" ? "Creating..." : "Create Key Result"}
        </button>
        {feedbackMessage && <span className={`form-message ${feedbackState}`}>{feedbackMessage}</span>}
      </div>
    </form>
  );
}
