"use client";

import { useCallback, useState } from "react";
import IssueComments from "./IssueComments";
import IssueAttachments from "./IssueAttachments";
import IssueActivity from "./IssueActivity";
import { getIssueActivity } from "../../lib/api/workspace";
import type { IssueActivity as IssueActivityEntry, IssueAttachment, IssueComment } from "../../types/domain/workspace";

type IssueActivitySectionProps = {
  issueId: number;
  initialComments: IssueComment[];
  initialAttachments: IssueAttachment[];
  initialActivity: IssueActivityEntry[];
  activityRestricted?: boolean;
};

export default function IssueActivitySection({
  issueId,
  initialComments,
  initialAttachments,
  initialActivity,
  activityRestricted
}: IssueActivitySectionProps) {
  const [activity, setActivity] = useState<IssueActivityEntry[]>(initialActivity);

  const refreshActivity = useCallback(async () => {
    try {
      const next = await getIssueActivity(issueId);
      setActivity(next);
    } catch {
      // keep the current activity list if refresh fails
    }
  }, [issueId]);

  return (
    <>
      <section className="section">
        <h3 className="section-title">Comments</h3>
        <IssueComments
          issueId={issueId}
          initialComments={initialComments}
          onActivityChange={refreshActivity}
        />
      </section>

      <section className="section">
        <h3 className="section-title">Attachments</h3>
        <IssueAttachments
          issueId={issueId}
          initialAttachments={initialAttachments}
          onActivityChange={refreshActivity}
        />
      </section>

      <section className="section">
        <h3 className="section-title">Activity</h3>
        <IssueActivity activities={activity} accessRestricted={activityRestricted} />
      </section>
    </>
  );
}
