"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { ApiError } from "../../lib/api/client";
import { PERMISSION_CODES } from "../../lib/auth/permissions";
import {
  deleteIssueAttachment,
  fetchIssueAttachmentDownload,
  fetchIssueAttachmentPreview,
  uploadIssueAttachment
} from "../../lib/api/workspace";
import { useAuth } from "../auth/AuthProvider";
import type { IssueAttachment } from "../../types/domain/workspace";

const MAX_UPLOAD_BYTES = 10 * 1024 * 1024;
const IMAGE_TYPES = ["image/png", "image/jpeg", "image/jpg", "image/webp"];

type IssueAttachmentsProps = {
  issueId: number;
  initialAttachments: IssueAttachment[];
  onActivityChange?: () => void;
};

type PreviewMap = Record<number, string>;

export default function IssueAttachments({ issueId, initialAttachments, onActivityChange }: IssueAttachmentsProps) {
  const { status, can, hasPermission } = useAuth();
  const [attachments, setAttachments] = useState<IssueAttachment[]>(initialAttachments);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [previews, setPreviews] = useState<PreviewMap>({});
  const previewRef = useRef<PreviewMap>({});

  const canView = hasPermission(PERMISSION_CODES.ISSUES_VIEW);
  const canUpload = can("issues.attachment.upload");
  const canDelete = can("issues.attachment.delete");

  const sortedAttachments = useMemo(
    () => [...attachments].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()),
    [attachments]
  );

  useEffect(() => {
    let active = true;
    const loadPreviews = async () => {
      const next: PreviewMap = {};
      for (const attachment of sortedAttachments) {
        if (!isPreviewable(attachment.mimeType)) {
          continue;
        }
        try {
          const blob = await fetchIssueAttachmentPreview(issueId, attachment.id);
          if (!active) {
            return;
          }
          next[attachment.id] = URL.createObjectURL(blob);
        } catch {
          // ignore preview errors
        }
      }
      if (active) {
        Object.values(previewRef.current).forEach((url) => URL.revokeObjectURL(url));
        previewRef.current = next;
        setPreviews(next);
      }
    };

    loadPreviews();

    return () => {
      active = false;
      Object.values(previewRef.current).forEach((url) => URL.revokeObjectURL(url));
      previewRef.current = {};
    };
  }, [issueId, sortedAttachments]);

  const handleUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    if (!canUpload || uploading) {
      return;
    }
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }
    if (file.size > MAX_UPLOAD_BYTES) {
      setError("Attachment exceeds 10 MB limit.");
      return;
    }
    setUploading(true);
    setError(null);
    try {
      const created = await uploadIssueAttachment(issueId, file);
      setAttachments((prev) => [created, ...prev]);
      event.target.value = "";
      onActivityChange?.();
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message || "Unable to upload attachment.");
      } else {
        setError("Unable to upload attachment.");
      }
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (attachmentId: number) => {
    if (!canDelete) {
      return;
    }
    setError(null);
    try {
      await deleteIssueAttachment(issueId, attachmentId);
      setAttachments((prev) => prev.filter((item) => item.id !== attachmentId));
      onActivityChange?.();
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message || "Unable to delete attachment.");
      } else {
        setError("Unable to delete attachment.");
      }
    }
  };

  const handleDownload = async (attachment: IssueAttachment) => {
    setError(null);
    try {
      const blob = await fetchIssueAttachmentDownload(issueId, attachment.id);
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = attachment.originalFilename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message || "Unable to download attachment.");
      } else {
        setError("Unable to download attachment.");
      }
    }
  };

  if (status === "loading") {
    return <p className="muted">Loading attachment permissions...</p>;
  }

  if (!canView) {
    return <p className="muted">You do not have permission to view attachments.</p>;
  }

  return (
    <div className="issue-attachments">
      {sortedAttachments.length === 0 ? (
        <p className="muted">No attachments yet. Upload a file to get started.</p>
      ) : (
        <div className="issue-attachments-list">
          {sortedAttachments.map((attachment) => (
            <div key={attachment.id} className="issue-attachment-card">
              <div className="issue-attachment-preview">
                {previews[attachment.id] ? (
                  <img
                    src={previews[attachment.id]}
                    alt={attachment.originalFilename}
                    className="issue-attachment-image"
                  />
                ) : (
                  <div className="issue-attachment-file">
                    <div className="issue-attachment-type">{formatMime(attachment.mimeType)}</div>
                    <div className="issue-attachment-size">{formatBytes(attachment.sizeBytes)}</div>
                  </div>
                )}
              </div>
              <div className="issue-attachment-body">
                <div className="issue-attachment-name">{attachment.originalFilename}</div>
                <div className="issue-attachment-meta">
                  Uploaded by {attachment.uploaderName} · {formatBytes(attachment.sizeBytes)} ·{" "}
                  {new Date(attachment.createdAt).toLocaleString()}
                </div>
                <div className="issue-attachment-actions">
                  <button type="button" className="link-button" onClick={() => handleDownload(attachment)}>
                    Download
                  </button>
                  {canDelete ? (
                    <button
                      type="button"
                      className="link-button destructive"
                      onClick={() => handleDelete(attachment.id)}
                    >
                      Delete
                    </button>
                  ) : null}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {error ? <div className="form-message error">{error}</div> : null}

      {canUpload ? (
        <div className="issue-attachment-upload">
          <label className="upload-field">
            <span className="muted">Upload attachment (max 10 MB)</span>
            <input
              type="file"
              onChange={handleUpload}
              disabled={uploading}
              accept="*/*"
            />
          </label>
          <div className="muted">{uploading ? "Uploading..." : "Select a file to upload."}</div>
        </div>
      ) : (
        <p className="muted">You do not have permission to upload attachments.</p>
      )}
    </div>
  );
}

function isPreviewable(mimeType: string) {
  return IMAGE_TYPES.includes(mimeType?.toLowerCase?.() ?? "");
}

function formatBytes(bytes: number) {
  if (!Number.isFinite(bytes)) {
    return "-";
  }
  if (bytes < 1024) {
    return `${bytes} B`;
  }
  const kb = bytes / 1024;
  if (kb < 1024) {
    return `${kb.toFixed(1)} KB`;
  }
  const mb = kb / 1024;
  return `${mb.toFixed(1)} MB`;
}

function formatMime(mimeType: string) {
  if (!mimeType) {
    return "FILE";
  }
  if (mimeType.startsWith("image/")) {
    return mimeType.replace("image/", "").toUpperCase();
  }
  const parts = mimeType.split("/");
  return parts[1]?.toUpperCase() ?? mimeType.toUpperCase();
}
