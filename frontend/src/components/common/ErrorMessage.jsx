import Button from '../ui/Button.jsx';

export default function ErrorMessage({ message, onRetry }) {
  return (
    <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-800">
      <div>{message}</div>
      {onRetry ? (
        <div className="mt-3">
          <Button size="sm" variant="outline" onClick={onRetry}>
            Retry
          </Button>
        </div>
      ) : null}
    </div>
  );
}
