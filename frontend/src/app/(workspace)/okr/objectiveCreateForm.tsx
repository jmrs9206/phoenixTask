"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { createOkrObjective } from "../../../lib/api/workspace";
import type { User } from "../../../types/domain/workspace";
import { useAuth } from "../../../components/auth/AuthProvider";

type ObjectiveCreateFormProps = {
  projectId: number;
  users: User[];
};

export default function ObjectiveCreateForm({ projectId, users }: ObjectiveCreateFormProps) {
  const { can } = useAuth();
  const canCreate = can("okr.objective.create");
  const canViewUsers = can("users.view");
  const hasUsers = users.length > 0;
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [ownerUserId, setOwnerUserId] = useState<number>(users[0]?.id ?? 0);
  const [status, setStatus] = useState("ACTIVE");
  const [periodStart, setPeriodStart] = useState("");
  const [periodEnd, setPeriodEnd] = useState("");
  const [state, setState] = useState<"idle" | "submitting" | "success" | "error">("idle");
  const [message, setMessage] = useState<string | null>(null);
  const feedbackMessage = !canCreate
    ? "You do not have permission to perform this action."
    : !canViewUsers
      ? "User directory access is required to create objectives."
      : !hasUsers
        ? "No users available to assign as owner."
        : message;
  const feedbackState = canCreate && canViewUsers && hasUsers ? state : "error";

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!canCreate || !hasUsers) {
      return;
    }
    setState("submitting");
    setMessage(null);
    try {
      if (!title.trim()) {
        throw new Error("Objective title is required");
      }
      if (!periodStart || !periodEnd) {
        throw new Error("Period start and end are required");
      }
      await createOkrObjective(projectId, {
        title: title.trim(),
        description: description.trim() || null,
        ownerUserId,
        status,
        periodStart,
        periodEnd
      });
      setState("success");
      setMessage("Saved successfully.");
      setTitle("");
      setDescription("");
      setStatus("ACTIVE");
      setPeriodStart("");
      setPeriodEnd("");
      router.refresh();
    } catch (error) {
      setState("error");
      setMessage("Please correct the highlighted fields.");
    }
  };

  const isDisabled = !canCreate || !canViewUsers || !hasUsers || state === "submitting";

  return (
    <form className="form form-columns" onSubmit={handleSubmit}>
      <label>
        Title
        <input
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          maxLength={200}
          required
          disabled={isDisabled}
        />
      </label>
      <label>
        Description
        <input
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          maxLength={500}
          disabled={isDisabled}
        />
      </label>
      <label>
        Owner
        <select
          value={ownerUserId}
          onChange={(event) => setOwnerUserId(Number(event.target.value))}
          disabled={isDisabled}
        >
          {users.map((user) => (
            <option key={user.id} value={user.id}>
              {user.firstName} {user.lastName}
            </option>
          ))}
        </select>
      </label>
      <label>
        Status
        <select value={status} onChange={(event) => setStatus(event.target.value)} disabled={isDisabled}>
          <option value="DRAFT">DRAFT</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="COMPLETED">COMPLETED</option>
          <option value="CANCELLED">CANCELLED</option>
        </select>
      </label>
      <label>
        Period start
        <input
          type="date"
          value={periodStart}
          onChange={(event) => setPeriodStart(event.target.value)}
          required
          disabled={isDisabled}
        />
      </label>
      <label>
        Period end
        <input
          type="date"
          value={periodEnd}
          onChange={(event) => setPeriodEnd(event.target.value)}
          required
          disabled={isDisabled}
        />
      </label>
      <div className="form-actions">
        <button type="submit" disabled={isDisabled}>
          {state === "submitting" ? "Creating..." : "Create Objective"}
        </button>
        {feedbackMessage && <span className={`form-message ${feedbackState}`}>{feedbackMessage}</span>}
      </div>
    </form>
  );
}
