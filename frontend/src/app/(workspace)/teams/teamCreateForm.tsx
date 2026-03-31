"use client";

import { useState } from "react";
import { createTeam } from "../../../lib/api/workspace";
import { useAuth } from "../../../components/auth/AuthProvider";

export default function CreateTeamForm() {
  const { can } = useAuth();
  const canCreate = can("teams.create");
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [status, setStatus] = useState<"idle" | "submitting" | "success" | "error">("idle");
  const [message, setMessage] = useState<string | null>(null);
  const feedbackMessage = canCreate ? message : "You do not have permission to perform this action.";
  const feedbackState = canCreate ? status : "error";

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!canCreate) {
      return;
    }
    setStatus("submitting");
    setMessage(null);
    try {
      if (!name.trim()) {
        throw new Error("Team name is required");
      }
      await createTeam({ name: name.trim(), description: description.trim() || null });
      setStatus("success");
      setMessage("Saved successfully.");
      setName("");
      setDescription("");
    } catch (error) {
      setStatus("error");
      setMessage("Please correct the highlighted fields.");
    }
  };

  return (
    <form className="form" onSubmit={handleSubmit}>
      <label>
        Team name
        <input
          value={name}
          onChange={(event) => setName(event.target.value)}
          required
          maxLength={120}
          disabled={!canCreate}
        />
      </label>
      <label>
        Description
        <input
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          maxLength={255}
          disabled={!canCreate}
        />
      </label>
      <div className="form-actions">
        <button type="submit" disabled={!canCreate || status === "submitting"}>
          {status === "submitting" ? "Creating..." : "Create Team"}
        </button>
        {feedbackMessage && <span className={`form-message ${feedbackState}`}>{feedbackMessage}</span>}
      </div>
    </form>
  );
}
