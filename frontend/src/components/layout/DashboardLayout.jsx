import { Outlet, useLocation } from 'react-router-dom';
import Navbar from './Navbar.jsx';
import Sidebar from './Sidebar.jsx';

const titles = {
  '/patient': 'Patient Dashboard',
  '/patient/book': 'Book Appointment',
  '/patient/appointments': 'My Appointments',
  '/patient/profile': 'Patient Profile',
  '/doctor': 'Doctor Dashboard',
  '/doctor/appointments': 'Doctor Appointments',
  '/doctor/profile': 'Doctor Profile',
  '/admin': 'Admin Dashboard',
  '/admin/patients': 'Patients Management',
  '/admin/doctors': 'Doctors Management',
  '/admin/appointments': 'Appointments Management',
};

export default function DashboardLayout() {
  const location = useLocation();
  const title = titles[location.pathname] || 'MediConnect Workspace';

  return (
    <div className="min-h-screen bg-slate-50 md:flex">
      <Sidebar />
      <div className="flex min-h-screen flex-1 flex-col">
        <Navbar title={title} subtitle="Role-aware clinical workspace connected to the live API." />
        <main className="flex-1 px-4 py-6 md:px-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
