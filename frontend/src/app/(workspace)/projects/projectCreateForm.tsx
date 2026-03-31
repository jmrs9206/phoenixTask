"use client";

import { useState } from "react";
import { createProject } from "../../../lib/api/workspace";
import { useAuth } from "../../../components/auth/AuthProvider";

export default function CreateProjectForm() {
  const { can } = useAuth();
  const canCreate = can("projects.create");
  const [projectKey, setProjectKey] = useState("");
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
      if (!projectKey.trim() || !name.trim()) {
        throw new Error("Project key and name are required");
      }
      await createProject({
        projectKey: projectKey.trim().toUpperCase(),
        name: name.trim(),
        description: description.trim() || null
      });
      setStatus("success");
      setMessage("Saved successfully.");
      setProjectKey("");
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
        Project key
        <input
          value={projectKey}
          onChange={(event) => setProjectKey(event.target.value)}
          required
          maxLength={16}
          placeholder="PTW"
          disabled={!canCreate}
        />
      </label>
      <label>
        Name
        <input
          value={name}
          onChange={(event) => setName(event.target.value)}
          required
          maxLength={200}
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
          {status === "submitting" ? "Creating..." : "Create Project"}
        </button>
        {feedbackMessage && <span className={`form-message ${feedbackState}`}>{feedbackMessage}</span>}
      </div>
    </form>
  );
}
