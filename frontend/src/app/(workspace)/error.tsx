"use client";

import ErrorState from "../../components/ui/ErrorState";

type ErrorProps = {
  error: Error & { digest?: string };
  reset: () => void;
};

export default function Error({ error, reset }: ErrorProps) {
  const isForbidden = error?.message === "Access denied";
  if (isForbidden) {
    return (
      <ErrorState
        title="Access restricted"
        message="You do not have permission to view this content."
        onRetry={reset}
      />
    );
  }
  return <ErrorState onRetry={reset} />;
}
