"use client";

import type { IssueCodeLink } from "../../types/domain/workspace";

type IssueDevActivityProps = {
  links: IssueCodeLink[];
};

const TYPE_LABELS: Record<string, string> = {
  BRANCH: "Branch",
  COMMIT: "Commit",
  PULL_REQUEST: "Pull Request",
  MERGE_REQUEST: "Merge Request"
};

const PROVIDER_LABELS: Record<string, string> = {
  GITHUB: "GitHub",
  GITLAB: "GitLab"
};

function formatTimestamp(value?: string | null) {
  if (!value) {
    return "—";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
}

function formatTitle(link: IssueCodeLink) {
  if (link.title && link.title.trim().length > 0) {
    return link.title;
  }
  return link.externalId;
}

export default function IssueDevActivity({ links }: IssueDevActivityProps) {
  if (!links || links.length === 0) {
    return (
      <div className="issue-placeholder">
        <p>No development activity linked yet. Add an issue key to branches, commits, or PRs to start linking.</p>
      </div>
    );
  }

  return (
    <div className="issue-dev-activity">
      {links.map((link) => {
        const label = TYPE_LABELS[link.artifactType] ?? link.artifactType;
        const provider = PROVIDER_LABELS[link.provider] ?? link.provider;
        const title = formatTitle(link);
        const time = formatTimestamp(link.externalCreatedAt ?? link.createdAt);
        return (
          <div key={link.id} className="issue-dev-item">
            <div className="issue-dev-header">
              <span className="issue-dev-type">{label}</span>
              <span className="issue-dev-time">{time}</span>
            </div>
            <div className="issue-dev-title">
              {link.url ? (
                <a href={link.url} target="_blank" rel="noreferrer">
                  {title}
                </a>
              ) : (
                title
              )}
            </div>
            <div className="issue-dev-meta">
              <span className="issue-dev-pill">{provider}</span>
              <span className="issue-dev-pill">
                {link.repoOwner}/{link.repoName}
              </span>
              <span className="issue-dev-id">{link.externalId}</span>
              {link.authorName ? <span className="issue-dev-author">{link.authorName}</span> : null}
            </div>
          </div>
        );
      })}
    </div>
  );
}
