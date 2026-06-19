import Spinner from '../ui/Spinner.jsx';

export default function LoadingSpinner({ fullscreen = false, label = 'Loading...' }) {
  const content = (
    <div className="flex items-center gap-3 text-sm text-slate-600">
      <Spinner className="h-5 w-5 text-primary" />
      <span>{label}</span>
    </div>
  );

  if (fullscreen) {
    return <div className="flex min-h-screen items-center justify-center bg-slate-50">{content}</div>;
  }

  return content;
}
