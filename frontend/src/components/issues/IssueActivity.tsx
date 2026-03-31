import type { IssueActivity } from "../../types/domain/workspace";

const EVENT_LABELS: Record<string, string> = {
  ISSUE_CREATED: "Issue created",
  COMMENT_CREATED: "Comment added",
  COMMENT_DELETED: "Comment deleted",
  ATTACHMENT_UPLOADED: "Attachment uploaded",
  ATTACHMENT_DELETED: "Attachment deleted"
};

type IssueActivityProps = {
  activities: IssueActivity[];
  accessRestricted?: boolean;
};

export default function IssueActivity({ activities, accessRestricted }: IssueActivityProps) {
  if (accessRestricted) {
    return <p className="muted">You do not have permission to view activity.</p>;
  }
  if (!activities || activities.length === 0) {
    return <p className="muted">No activity recorded yet.</p>;
  }

  return (
    <div className="issue-activity">
      {activities.map((event) => (
        <div key={event.id} className="issue-activity-item">
          <div className="issue-activity-marker" />
          <div className="issue-activity-content">
            <div className="issue-activity-header">
              <span className="issue-activity-title">
                {EVENT_LABELS[event.eventType] ?? event.eventType}
              </span>
              <span className="issue-activity-time">
                {new Date(event.createdAt).toLocaleString()}
              </span>
            </div>
            <div className="issue-activity-meta">
              <span className="issue-activity-actor">{event.actorName}</span>
              {renderDetail(event)}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

function renderDetail(event: IssueActivity) {
  const metadata = event.metadata ?? {};
  if (event.eventType.startsWith("COMMENT")) {
    const preview = metadata.preview as string | undefined;
    if (preview) {
      return <span className="issue-activity-detail">“{preview}”</span>;
    }
  }
  if (event.eventType.startsWith("ATTACHMENT")) {
    const filename = metadata.filename as string | undefined;
    const size = metadata.sizeBytes as number | undefined;
    const sizeLabel = typeof size === "number" ? formatBytes(size) : null;
    if (filename) {
      return (
        <span className="issue-activity-detail">
          {filename}{sizeLabel ? ` · ${sizeLabel}` : ""}
        </span>
      );
    }
  }
  if (event.eventType === "ISSUE_CREATED") {
    const issueKey = metadata.issueKey as string | undefined;
    if (issueKey) {
      return <span className="issue-activity-detail">{issueKey}</span>;
    }
  }
  return null;
}

function formatBytes(bytes: number) {
  if (bytes < 1024) return `${bytes} B`;
  const kb = bytes / 1024;
  if (kb < 1024) return `${kb.toFixed(1)} KB`;
  return `${(kb / 1024).toFixed(1)} MB`;
}
