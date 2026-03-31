export default function LoadingState() {
  return (
    <div className="state">
      <h3>Loading</h3>
      <p>Fetching the latest data from PhoenixTask®.</p>
      <div className="loading-block">
        <div className="loading-skeleton" />
        <div className="loading-skeleton" />
        <div className="loading-skeleton" />
      </div>
    </div>
  );
}
