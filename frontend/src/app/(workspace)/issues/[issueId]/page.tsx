import { notFound } from "next/navigation";
import {
  getIssueActivity,
  getIssueAttachments,
  getIssueCodeLinks,
  getIssueComments,
  getIssueDetail
} from "../../../../lib/api/workspace.server";
import { ApiError } from "../../../../lib/api/client";
import PageHeader from "../../../../components/ui/PageHeader";
import KeyValueList from "../../../../components/ui/KeyValueList";
import IssueActivitySection from "../../../../components/issues/IssueActivitySection";
import IssueDevActivity from "../../../../components/issues/IssueDevActivity";
import type {
  IssueActivity,
  IssueAttachment,
  IssueCodeLink,
  IssueComment
} from "../../../../types/domain/workspace";

type IssuePageProps = {
  params: Promise<{ issueId: string }>;
};

async function safeFetch<T>(promise: Promise<T>, fallback: T): Promise<{ data: T; restricted: boolean }> {
  try {
    return { data: await promise, restricted: false };
  } catch (error) {
    if (error instanceof ApiError && error.status === 403) {
      return { data: fallback, restricted: true };
    }
    throw error;
  }
}

export default async function IssueDetailPage({ params }: IssuePageProps) {
  const { issueId } = await params;
  const issueIdNumber = Number(issueId);
  if (Number.isNaN(issueIdNumber)) {
    notFound();
  }

  const [issue, commentsResult, attachmentsResult, activityResult, codeLinksResult] = await Promise.all([
    getIssueDetail(issueIdNumber),
    safeFetch(getIssueComments(issueIdNumber), [] as IssueComment[]),
    safeFetch(getIssueAttachments(issueIdNumber), [] as IssueAttachment[]),
    safeFetch(getIssueActivity(issueIdNumber), [] as IssueActivity[]),
    safeFetch(getIssueCodeLinks(issueIdNumber), [] as IssueCodeLink[])
  ]);
  const comments = commentsResult.data;
  const attachments = attachmentsResult.data;
  const activity = activityResult.data;
  const codeLinks = codeLinksResult.data;
  const activityRestricted = activityResult.restricted;
  const codeLinksRestricted = codeLinksResult.restricted;
  const dueDateLabel = issue.dueDate
    ? new Date(issue.dueDate).toLocaleDateString()
    : "Not set";

  return (
    <div className="page">
      <PageHeader title={issue.title} subtitle={`${issue.issueKey} · ${issue.projectKey}`} />

      <section className="section">
        <h3 className="section-title">Summary</h3>
        <div className="issue-detail-grid">
          <div className="issue-summary">
            <div className="issue-summary-eyebrow">
              <span className="issue-key-badge">{issue.issueKey}</span>
              <span className="issue-project">Project {issue.projectKey}</span>
            </div>
            <h2 className="issue-title">{issue.title}</h2>
            <p className="issue-summary-note">Reported by {issue.reporterName}</p>
          </div>
          <div className="issue-meta-card">
            <div className="issue-meta-row">
              <span className="issue-meta-label">Status</span>
              <span className="issue-pill">{issue.status}</span>
            </div>
            <div className="issue-meta-row">
              <span className="issue-meta-label">Priority</span>
              <span className="issue-pill">{issue.priority}</span>
            </div>
            <div className="issue-meta-row">
              <span className="issue-meta-label">Assignee</span>
              <span className="issue-pill">{issue.assigneeName}</span>
            </div>
            <div className="issue-meta-row">
              <span className="issue-meta-label">Due date</span>
              <span className="issue-pill">{dueDateLabel}</span>
            </div>
          </div>
        </div>
      </section>

      <section className="section">
        <h3 className="section-title">Description</h3>
        <p className="description-text">{issue.description ?? "No description provided."}</p>
      </section>

      <section className="section">
        <h3 className="section-title">Context & Metadata</h3>
        <KeyValueList
          items={[
            { label: "Project", value: issue.projectKey },
            { label: "Reporter", value: issue.reporterName },
            { label: "Assignee", value: issue.assigneeName },
            { label: "Due date", value: dueDateLabel },
            { label: "Created", value: new Date(issue.createdAt).toLocaleString() }
          ]}
        />
      </section>

      <section className="section">
        <h3 className="section-title">Development Activity</h3>
        {codeLinksRestricted ? (
          <div className="issue-placeholder">
            <p>You do not have permission to view development activity.</p>
          </div>
        ) : (
          <IssueDevActivity links={codeLinks} />
        )}
      </section>

      <IssueActivitySection
        issueId={issueIdNumber}
        initialComments={comments}
        initialAttachments={attachments}
        initialActivity={activity}
        activityRestricted={activityRestricted}
      />

      <section className="section">
        <h3 className="section-title">Relationships</h3>
        <div className="issue-placeholder">
          <p>Reserved for next batch: dependencies, linked issues, and blocking state.</p>
        </div>
      </section>
    </div>
  );
}
