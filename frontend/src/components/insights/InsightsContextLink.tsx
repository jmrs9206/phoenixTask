"use client";

import Link from "next/link";
import { useAuth } from "../auth/AuthProvider";

type InsightsContextLinkProps = {
  href: string;
  label?: string;
  description?: string;
};

export default function InsightsContextLink({
  href,
  label = "View Insights",
  description
}: InsightsContextLinkProps) {
  const { can } = useAuth();
  if (!can("analytics.view")) {
    return null;
  }

  return (
    <div className="insights-context">
      <Link href={href} className="insights-context-link">
        {label}
      </Link>
      {description ? <span className="insights-context-desc">{description}</span> : null}
    </div>
  );
}
