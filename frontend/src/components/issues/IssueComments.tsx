"use client";

import { useMemo, useState } from "react";
import { PERMISSION_CODES } from "../../lib/auth/permissions";
import { createIssueComment, deleteIssueComment } from "../../lib/api/workspace";
import type { IssueComment } from "../../types/domain/workspace";
import { useAuth } from "../auth/AuthProvider";
import { ApiError } from "../../lib/api/client";

type IssueCommentsProps = {
  issueId: number;
  initialComments: IssueComment[];
  onActivityChange?: () => void;
};

export default function IssueComments({ issueId, initialComments, onActivityChange }: IssueCommentsProps) {
  const { status, user, can, hasPermission } = useAuth();
  const [comments, setComments] = useState<IssueComment[]>(initialComments);
  const [body, setBody] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const canView = hasPermission(PERMISSION_CODES.ISSUES_COMMENT_VIEW);
  const canCreate = can("issues.comment.create");
  const canDelete = can("issues.comment.delete");

  const sortedComments = useMemo(
    () => [...comments].sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()),
    [comments]
  );

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!canCreate || submitting) {
      return;
    }
    const content = body.trim();
    if (!content) {
      setError("Comment body is required.");
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const created = await createIssueComment(issueId, content);
      setComments((prev) => [...prev, created]);
      setBody("");
      onActivityChange?.();
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message || "Unable to post comment.");
      } else {
        setError("Unable to post comment.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (commentId: number) => {
    if (!canDelete) {
      return;
    }
    setError(null);
    try {
      await deleteIssueComment(issueId, commentId);
      setComments((prev) => prev.filter((comment) => comment.id !== commentId));
      onActivityChange?.();
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message || "Unable to delete comment.");
      } else {
        setError("Unable to delete comment.");
      }
    }
  };

  if (status === "loading") {
    return <p className="muted">Loading comment permissions...</p>;
  }

  if (!canView) {
    return <p className="muted">You do not have permission to view comments.</p>;
  }

  return (
    <div className="issue-comments">
      {sortedComments.length === 0 ? (
        <p className="muted">No comments yet. Be the first to add one.</p>
      ) : (
        <div className="issue-comments-list">
          {sortedComments.map((comment) => (
            <div key={comment.id} className="issue-comment">
              <div className="issue-comment-header">
                <div>
                  <div className="issue-comment-author">{comment.authorName}</div>
                  <div className="issue-comment-time">
                    {new Date(comment.createdAt).toLocaleString()}
                  </div>
                </div>
                {canDelete ? (
                  <button
                    type="button"
                    className="link-button"
                    onClick={() => handleDelete(comment.id)}
                  >
                    Delete
                  </button>
                ) : null}
              </div>
              <p className="issue-comment-body">{comment.body}</p>
            </div>
          ))}
        </div>
      )}

      {error ? <div className="form-message error">{error}</div> : null}

      {canCreate ? (
        <form className="issue-comment-form" onSubmit={handleSubmit}>
          <label>
            <span className="muted">Add a comment</span>
            <textarea
              rows={3}
              value={body}
              onChange={(event) => setBody(event.target.value)}
              placeholder={user ? `Comment as ${user.firstName} ${user.lastName}` : "Write a comment"}
              maxLength={2000}
              required
            />
          </label>
          <div className="form-actions">
            <button type="submit" disabled={submitting}>
              {submitting ? "Posting..." : "Post comment"}
            </button>
          </div>
        </form>
      ) : (
        <p className="muted">You do not have permission to add comments.</p>
      )}
    </div>
  );
}
