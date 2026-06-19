export const ROUTES = {
  home: '/',
  login: '/login',
  register: '/register',
  patientDashboard: '/patient',
  patientAppointments: '/patient/appointments',
  patientBook: '/patient/book',
  patientProfile: '/patient/profile',
  doctorDashboard: '/doctor',
  doctorAppointments: '/doctor/appointments',
  doctorProfile: '/doctor/profile',
  adminDashboard: '/admin',
  adminPatients: '/admin/patients',
  adminDoctors: '/admin/doctors',
  adminAppointments: '/admin/appointments',
};

export function getDashboardRoute(role) {
  switch (role) {
    case 'ADMIN':
      return ROUTES.adminDashboard;
    case 'DOCTOR':
      return ROUTES.doctorDashboard;
    case 'PATIENT':
    default:
      return ROUTES.patientDashboard;
  }
}
