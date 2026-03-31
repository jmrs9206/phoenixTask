"use client";

import { useEffect, useState } from "react";
import { createIssue, getProjects, getUsers } from "../../../lib/api/workspace";
import { useAuth } from "../../../components/auth/AuthProvider";
import { ApiError } from "../../../lib/api/client";
import type { Project, User } from "../../../types/domain/workspace";

const statusOptions = ["OPEN", "IN_PROGRESS", "BLOCKED", "DONE"] as const;
const priorityOptions = ["LOW", "MEDIUM", "HIGH", "CRITICAL"] as const;

export default function IssueCreateForm() {
  const { can } = useAuth();
  const canCreate = can("issues.create");
  const canViewUsers = can("users.view");
  const canViewProjects = can("projects.view");
  const [projects, setProjects] = useState<Project[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const activeUsers = users.filter((user) => user.status === "ACTIVE");
  const [projectId, setProjectId] = useState("");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [reporterUserId, setReporterUserId] = useState("");
  const [assigneeUserId, setAssigneeUserId] = useState("");
  const [statusValue, setStatusValue] = useState<(typeof statusOptions)[number]>("OPEN");
  const [priorityValue, setPriorityValue] = useState<(typeof priorityOptions)[number]>("MEDIUM");
  const [status, setStatus] = useState<"idle" | "submitting" | "success" | "error">("idle");
  const [message, setMessage] = useState<string | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const accessMessage = canCreate
    ? (!canViewProjects && !canViewUsers
      ? "Project and user directory access are required to create issues."
      : !canViewProjects
        ? "Project access is required to create issues."
        : !canViewUsers
          ? "User directory access is required to assign reporter or assignee."
          : null)
    : null;
  const feedbackMessage = canCreate ? message : "You do not have permission to perform this action.";
  const feedbackState = canCreate ? status : "error";
  const isFormDisabled = loading || !canCreate || accessMessage !== null;

  useEffect(() => {
    let isMounted = true;
    const load = async () => {
      if (!canCreate) {
        if (isMounted) {
          setLoading(false);
          setLoadError(null);
        }
        return;
      }
      if (accessMessage) {
        if (isMounted) {
          setProjects([]);
          setUsers([]);
          setLoadError(null);
          setLoading(false);
        }
        return;
      }
      try {
        const results = await Promise.allSettled([
          getProjects({ pageSize: 100 }),
          getUsers()
        ]);
        const [projectsResult, usersResult] = results;
        if (isMounted) {
          if (projectsResult.status === "fulfilled") {
            setProjects(projectsResult.value.items);
          } else if (projectsResult.reason instanceof ApiError && projectsResult.reason.status === 403) {
            setLoadError("Project access is required to create issues.");
          } else {
            throw projectsResult.reason;
          }
          if (usersResult.status === "fulfilled") {
            setUsers(usersResult.value);
          } else if (usersResult.reason instanceof ApiError && usersResult.reason.status === 403) {
            setLoadError((current) => current ?? "User directory access is required to assign reporter or assignee.");
          } else {
            throw usersResult.reason;
          }
        }
      } catch (error) {
        if (isMounted) {
          if (error instanceof ApiError && error.status === 403) {
            setLoadError(null);
          } else {
            setLoadError("We couldn't load this data. Please try again.");
          }
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };
    load();
    return () => {
      isMounted = false;
    };
  }, [canCreate, accessMessage]);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!canCreate) {
      return;
    }
    if (isFormDisabled) {
      return;
    }
    setStatus("submitting");
    setMessage(null);
    try {
    if (!projectId || !reporterUserId || !title.trim()) {
      throw new Error("Project, title, and reporter are required");
    }
      await createIssue({
        projectId: Number(projectId),
        title: title.trim(),
        description: description.trim() || null,
        reporterUserId: Number(reporterUserId),
        assigneeUserId: assigneeUserId ? Number(assigneeUserId) : null,
        status: statusValue,
        priority: priorityValue
      });
      setStatus("success");
      setMessage("Saved successfully.");
      setProjectId("");
      setTitle("");
      setDescription("");
      setReporterUserId("");
      setAssigneeUserId("");
      setStatusValue(statusOptions[0]);
      setPriorityValue(priorityOptions[1]);
    } catch (error) {
      setStatus("error");
      setMessage("Please correct the highlighted fields.");
    }
  };

  return (
    <form className="form form-columns" onSubmit={handleSubmit}>
      {(accessMessage || loadError) && <div className="form-message error">{accessMessage ?? loadError}</div>}
      <label>
        Project
        <select
          value={projectId}
          onChange={(event) => setProjectId(event.target.value)}
          required
          disabled={isFormDisabled}
        >
          <option value="" disabled>
            Select project
          </option>
          {projects.map((project) => (
            <option key={project.id} value={project.id}>
              {project.projectKey} — {project.name}
            </option>
          ))}
        </select>
      </label>
      <label>
        Title
        <input
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          required
          maxLength={200}
          disabled={isFormDisabled}
        />
      </label>
      <label>
        Description
        <input
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          maxLength={1000}
          disabled={isFormDisabled}
        />
      </label>
      <label>
        Reporter
        <select
          value={reporterUserId}
          onChange={(event) => setReporterUserId(event.target.value)}
          required
          disabled={isFormDisabled}
        >
          <option value="" disabled>
            Select reporter
          </option>
          {activeUsers.map((user) => (
            <option key={user.id} value={user.id}>
              {user.firstName} {user.lastName}
            </option>
          ))}
        </select>
      </label>
      <label>
        Assignee
        <select
          value={assigneeUserId}
          onChange={(event) => setAssigneeUserId(event.target.value)}
          disabled={isFormDisabled}
        >
          <option value="">Unassigned</option>
          {activeUsers.map((user) => (
            <option key={user.id} value={user.id}>
              {user.firstName} {user.lastName}
            </option>
          ))}
        </select>
      </label>
      <label>
        Status
        <select
          value={statusValue}
          onChange={(event) => setStatusValue(event.target.value as (typeof statusOptions)[number])}
          disabled={isFormDisabled}
        >
          {statusOptions.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      </label>
      <label>
        Priority
        <select
          value={priorityValue}
          onChange={(event) => setPriorityValue(event.target.value as (typeof priorityOptions)[number])}
          disabled={isFormDisabled}
        >
          {priorityOptions.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      </label>
      <div className="form-actions">
        <button type="submit" disabled={isFormDisabled || status === "submitting"}>
          {status === "submitting" ? "Creating..." : "Create Issue"}
        </button>
        {feedbackMessage && <span className={`form-message ${feedbackState}`}>{feedbackMessage}</span>}
      </div>
    </form>
  );
}
