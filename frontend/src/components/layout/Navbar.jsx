import { Bell } from 'lucide-react';

export default function Navbar({ title, subtitle }) {
  return (
    <div className="sticky top-0 z-20 border-b border-slate-200 bg-white/95 backdrop-blur">
      <div className="flex items-center justify-between px-6 py-4">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">{title}</h1>
          {subtitle ? <p className="text-sm text-slate-600">{subtitle}</p> : null}
        </div>
        <button
          type="button"
          className="flex h-11 w-11 items-center justify-center rounded-md border border-slate-200 text-slate-500 transition hover:bg-slate-50"
        >
          <Bell className="h-5 w-5" />
        </button>
      </div>
    </div>
  );
}
