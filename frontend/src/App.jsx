import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './auth/ProtectedRoute.jsx';
import DashboardLayout from './components/layout/DashboardLayout.jsx';
import PublicLayout from './components/layout/PublicLayout.jsx';
import { ROUTES } from './constants/routes.js';
import AdminDashboard from './pages/admin/AdminDashboard.jsx';
import AppointmentsManagement from './pages/admin/AppointmentsManagement.jsx';
import DoctorsManagement from './pages/admin/DoctorsManagement.jsx';
import PatientsManagement from './pages/admin/PatientsManagement.jsx';
import DoctorAppointments from './pages/doctor/DoctorAppointments.jsx';
import DoctorDashboard from './pages/doctor/DoctorDashboard.jsx';
import DoctorProfile from './pages/doctor/DoctorProfile.jsx';
import PatientDashboard from './pages/patient/PatientDashboard.jsx';
import BookAppointment from './pages/patient/BookAppointment.jsx';
import MyAppointments from './pages/patient/MyAppointments.jsx';
import PatientProfile from './pages/patient/PatientProfile.jsx';
import LandingPage from './pages/public/LandingPage.jsx';
import LoginPage from './pages/public/LoginPage.jsx';
import RegisterPage from './pages/public/RegisterPage.jsx';
import NotFound from './pages/public/NotFound.jsx';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<PublicLayout />}>
          <Route path={ROUTES.home} element={<LandingPage />} />
          <Route path={ROUTES.login} element={<LoginPage />} />
          <Route path={ROUTES.register} element={<RegisterPage />} />
        </Route>

        <Route element={<ProtectedRoute roles={['PATIENT']} />}>
          <Route element={<DashboardLayout />}>
            <Route path={ROUTES.patientDashboard} element={<PatientDashboard />} />
            <Route path={ROUTES.patientBook} element={<BookAppointment />} />
            <Route path={ROUTES.patientAppointments} element={<MyAppointments />} />
            <Route path={ROUTES.patientProfile} element={<PatientProfile />} />
          </Route>
        </Route>

        <Route element={<ProtectedRoute roles={['DOCTOR']} />}>
          <Route element={<DashboardLayout />}>
            <Route path={ROUTES.doctorDashboard} element={<DoctorDashboard />} />
            <Route path={ROUTES.doctorAppointments} element={<DoctorAppointments />} />
            <Route path={ROUTES.doctorProfile} element={<DoctorProfile />} />
          </Route>
        </Route>

        <Route element={<ProtectedRoute roles={['ADMIN']} />}>
          <Route element={<DashboardLayout />}>
            <Route path={ROUTES.adminDashboard} element={<AdminDashboard />} />
            <Route path={ROUTES.adminPatients} element={<PatientsManagement />} />
            <Route path={ROUTES.adminDoctors} element={<DoctorsManagement />} />
            <Route path={ROUTES.adminAppointments} element={<AppointmentsManagement />} />
          </Route>
        </Route>

        <Route path="/dashboard" element={<Navigate to={ROUTES.home} replace />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
}
