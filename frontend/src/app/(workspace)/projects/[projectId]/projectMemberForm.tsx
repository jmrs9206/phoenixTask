"use client";

import { useState } from "react";
import { addProjectMember } from "../../../../lib/api/workspace";
import type { RoleOption, User } from "../../../../types/domain/workspace";
import { useAuth } from "../../../../components/auth/AuthProvider";

type Props = {
  projectId: number;
  roles: RoleOption[];
  users: User[];
  accessMessage?: string;
};

export default function AddProjectMemberForm({ projectId, roles, users, accessMessage }: Props) {
  const { can } = useAuth();
  const canAdd = can("project.members.add");
  const [userId, setUserId] = useState("");
  const [roleId, setRoleId] = useState("");
  const [status, setStatus] = useState<"idle" | "submitting" | "success" | "error">("idle");
  const [message, setMessage] = useState<string | null>(null);
  const options = users.filter((user) => user.status === "ACTIVE");
  const hasOptions = options.length > 0 && roles.length > 0;
  const isRestricted = Boolean(accessMessage);
  const feedbackMessage = !canAdd
    ? "You do not have permission to perform this action."
    : accessMessage ?? (!hasOptions ? "No eligible users or roles available." : message);
  const feedbackState = !canAdd || isRestricted || !hasOptions ? "error" : status;
  const isDisabled = !canAdd || isRestricted || !hasOptions;

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!canAdd) {
      return;
    }
    if (isRestricted || !hasOptions) {
      return;
    }
    setStatus("submitting");
    setMessage(null);
    try {
      if (!userId || !roleId) {
        throw new Error("User and role are required");
      }
      await addProjectMember(projectId, { userId: Number(userId), roleId: Number(roleId) });
      setStatus("success");
      setMessage("Saved successfully.");
      setUserId("");
      setRoleId("");
    } catch (error) {
      setStatus("error");
      setMessage("Please correct the highlighted fields.");
    }
  };

  return (
    <form className="form" onSubmit={handleSubmit}>
      <label>
        User
        <select value={userId} onChange={(event) => setUserId(event.target.value)} required disabled={isDisabled}>
          <option value="" disabled>
            Select user
          </option>
          {options.map((user) => (
            <option key={user.id} value={user.id}>
              {user.firstName} {user.lastName} ({user.email})
            </option>
          ))}
        </select>
      </label>
      <label>
        Role
        <select value={roleId} onChange={(event) => setRoleId(event.target.value)} required disabled={isDisabled}>
          <option value="" disabled>
            Select role
          </option>
          {roles.map((role) => (
            <option key={role.id} value={role.id}>
              {role.name}
            </option>
          ))}
        </select>
      </label>
      <div className="form-actions">
        <button type="submit" disabled={isDisabled || status === "submitting"}>
          {status === "submitting" ? "Adding..." : "Add Member"}
        </button>
        {feedbackMessage && <span className={`form-message ${feedbackState}`}>{feedbackMessage}</span>}
      </div>
    </form>
  );
}
