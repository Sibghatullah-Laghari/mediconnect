import { Link } from 'react-router-dom';
import Button from '../ui/Button.jsx';

export default function EmptyState({ title, description, ctaLabel, ctaTo }) {
  return (
    <div className="rounded-lg border border-dashed border-slate-300 bg-white p-8 text-center">
      <h3 className="text-lg font-medium text-slate-900">{title}</h3>
      <p className="mt-2 text-sm text-slate-600">{description}</p>
      {ctaLabel && ctaTo ? (
        <div className="mt-5">
          <Link to={ctaTo}>
            <Button>{ctaLabel}</Button>
          </Link>
        </div>
      ) : null}
    </div>
  );
}
