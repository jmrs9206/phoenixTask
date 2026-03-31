type ErrorStateProps = {
  onRetry?: () => void;
  title?: string;
  message?: string;
};

export default function ErrorState({ onRetry, title, message }: ErrorStateProps) {
  const heading = title ?? "Something went wrong";
  const body = message ?? "We couldn’t load this data. Please try again.";
  return (
    <div className="state">
      <h3>{heading}</h3>
      <p>{body}</p>
      {onRetry ? (
        <button type="button" onClick={onRetry}>
          Try again
        </button>
      ) : null}
    </div>
  );
}
