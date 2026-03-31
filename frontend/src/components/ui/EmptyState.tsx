type EmptyStateProps = {
  title?: string;
  message?: string;
  actionLabel?: string;
  onAction?: () => void;
  size?: "default" | "compact";
};

export default function EmptyState({
  title = "No data yet",
  message = "Nothing to show yet.",
  actionLabel,
  onAction,
  size = "default"
}: EmptyStateProps) {
  const className = size === "compact" ? "state state--compact" : "state";

  return (
    <div className={className}>
      <h3>{title}</h3>
      <p>{message}</p>
      {actionLabel && onAction ? (
        <button type="button" onClick={onAction}>
          {actionLabel}
        </button>
      ) : null}
    </div>
  );
}
