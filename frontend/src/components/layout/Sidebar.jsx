import { CalendarDays, ClipboardList, LayoutDashboard, LogOut, Stethoscope, Users } from 'lucide-react';
import { NavLink } from 'react-router-dom';
import { ROUTES } from '../../constants/routes.js';
import { useAuth } from '../../auth/useAuth.js';

const navByRole = {
  PATIENT: [
    { to: ROUTES.patientDashboard, label: 'Overview', icon: LayoutDashboard, end: true },
    { to: ROUTES.patientBook, label: 'Book Appointment', icon: CalendarDays },
    { to: ROUTES.patientAppointments, label: 'My Appointments', icon: ClipboardList },
    { to: ROUTES.patientProfile, label: 'Profile', icon: Users },
  ],
  DOCTOR: [
    { to: ROUTES.doctorDashboard, label: 'Schedule', icon: LayoutDashboard, end: true },
    { to: ROUTES.doctorAppointments, label: 'Appointments', icon: ClipboardList },
    { to: ROUTES.doctorProfile, label: 'Profile', icon: Stethoscope },
  ],
  ADMIN: [
    { to: ROUTES.adminDashboard, label: 'Overview', icon: LayoutDashboard, end: true },
    { to: ROUTES.adminPatients, label: 'Patients', icon: Users },
    { to: ROUTES.adminDoctors, label: 'Doctors', icon: Stethoscope },
    { to: ROUTES.adminAppointments, label: 'Appointments', icon: ClipboardList },
  ],
};

export default function Sidebar() {
  const { user, logout } = useAuth();
  const items = navByRole[user?.role] || [];

  return (
    <aside className="hidden w-72 shrink-0 border-r border-slate-200 bg-white md:flex md:flex-col">
      <div className="px-6 py-6">
        <p className="text-xs uppercase tracking-wide text-slate-500">Signed in</p>
        <p className="mt-2 text-lg font-semibold text-slate-900">{user?.email}</p>
        <p className="text-sm text-slate-600">{user?.role}</p>
      </div>
      <nav className="flex-1 space-y-1 px-4">
        {items.map(({ to, label, icon: Icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `flex min-h-11 items-center gap-3 rounded-md px-3 py-2 text-sm ${
                isActive ? 'bg-blue-50 text-primary' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
              }`
            }
          >
            <Icon className="h-4 w-4" />
            {label}
          </NavLink>
        ))}
      </nav>
      <div className="border-t border-slate-200 p-4">
        <button
          type="button"
          onClick={logout}
          className="flex min-h-11 w-full items-center gap-3 rounded-md px-3 py-2 text-sm text-slate-600 transition hover:bg-slate-50 hover:text-slate-900"
        >
          <LogOut className="h-4 w-4" />
          Logout
        </button>
      </div>
    </aside>
  );
}
