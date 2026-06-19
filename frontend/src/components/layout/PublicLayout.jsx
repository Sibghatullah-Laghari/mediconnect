import { Activity } from 'lucide-react';
import { Link, Outlet } from 'react-router-dom';
import { ROUTES } from '../../constants/routes.js';

export default function PublicLayout() {
  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <Link to={ROUTES.home} className="flex items-center gap-3 text-slate-900">
            <span className="flex h-10 w-10 items-center justify-center rounded-md bg-primary text-white">
              <Activity className="h-5 w-5" />
            </span>
            <div>
              <p className="text-sm font-semibold">MediConnect</p>
              <p className="text-xs text-slate-500">Healthcare operations</p>
            </div>
          </Link>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-6 py-10">
        <Outlet />
      </main>
    </div>
  );
}
