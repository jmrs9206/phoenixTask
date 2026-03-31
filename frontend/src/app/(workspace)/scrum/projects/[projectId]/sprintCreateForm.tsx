"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { createSprint } from "../../../../../lib/api/workspace";
import { useAuth } from "../../../../../components/auth/AuthProvider";

type SprintCreateFormProps = {
  projectId: number;
};

export default function SprintCreateForm({ projectId }: SprintCreateFormProps) {
  const { can } = useAuth();
  const canCreate = can("scrum.sprint.create");
  const router = useRouter();
  const [name, setName] = useState("");
  const [goal, setGoal] = useState("");
  const [status, setStatus] = useState("PLANNED");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
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
      if (!name.trim()) {
        throw new Error("Sprint name is required");
      }
      if (!startDate || !endDate) {
        throw new Error("Start and end dates are required");
      }
      if (startDate > endDate) {
        throw new Error("Start date must be before end date");
      }
      await createSprint(projectId, {
        name: name.trim(),
        goal: goal.trim() || null,
        status,
        startDate,
        endDate
      });
      setState("success");
      setMessage("Saved successfully.");
      setName("");
      setGoal("");
      setStatus("PLANNED");
      setStartDate("");
      setEndDate("");
      router.refresh();
    } catch (error) {
      setState("error");
      setMessage("Please correct the highlighted fields.");
    }
  };

  return (
    <form className="form form-columns" onSubmit={handleSubmit}>
      <label>
        Sprint name
        <input
          value={name}
          onChange={(event) => setName(event.target.value)}
          maxLength={120}
          required
          disabled={!canCreate}
        />
      </label>
      <label>
        Goal
        <input
          value={goal}
          onChange={(event) => setGoal(event.target.value)}
          maxLength={255}
          disabled={!canCreate}
        />
      </label>
      <label>
        Status
        <select value={status} onChange={(event) => setStatus(event.target.value)} disabled={!canCreate}>
          <option value="PLANNED">PLANNED</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="COMPLETED">COMPLETED</option>
        </select>
      </label>
      <label>
        Start date
        <input
          type="date"
          value={startDate}
          onChange={(event) => setStartDate(event.target.value)}
          required
          disabled={!canCreate}
        />
      </label>
      <label>
        End date
        <input
          type="date"
          value={endDate}
          onChange={(event) => setEndDate(event.target.value)}
          required
          disabled={!canCreate}
        />
      </label>
      <div className="form-actions">
        <button type="submit" disabled={!canCreate || state === "submitting"}>
          {state === "submitting" ? "Creating..." : "Create Sprint"}
        </button>
        {feedbackMessage && <span className={`form-message ${feedbackState}`}>{feedbackMessage}</span>}
      </div>
    </form>
  );
}
