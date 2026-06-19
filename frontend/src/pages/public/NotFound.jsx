import { Link } from 'react-router-dom';
import Button from '../../components/ui/Button.jsx';
import { ROUTES } from '../../constants/routes.js';

export default function NotFound() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center text-center">
      <h1 className="text-3xl font-semibold text-slate-900">Page not found</h1>
      <p className="mt-3 text-sm text-slate-600">The requested route does not exist in this frontend workspace.</p>
      <Link className="mt-6" to={ROUTES.home}>
        <Button>Return home</Button>
      </Link>
    </div>
  );
}
