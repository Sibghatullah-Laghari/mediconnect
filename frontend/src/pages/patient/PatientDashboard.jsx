import { CalendarDays, ClipboardList, Stethoscope } from 'lucide-react';
import EmptyState from '../../components/common/EmptyState.jsx';
import LoadingSpinner from '../../components/common/LoadingSpinner.jsx';
import PageHeader from '../../components/common/PageHeader.jsx';
import StatCard from '../../components/common/StatCard.jsx';
import { ROUTES } from '../../constants/routes.js';
import { useAppointments } from '../../hooks/useAppointments.js';
import { useDoctors } from '../../hooks/useDoctors.js';
import { usePatientProfile } from '../../hooks/usePatients.js';
import { isUpcomingAppointment } from '../../utils/dateUtils.js';

export default function PatientDashboard() {
  const patientProfile = usePatientProfile();
  const appointments = useAppointments(patientProfile.isSuccess);
  const doctors = useDoctors();

  if (patientProfile.isLoading || appointments.isLoading || doctors.isLoading) {
    return <LoadingSpinner label="Loading patient dashboard" />;
  }

  if (!patientProfile.data) {
    return (
      <EmptyState
        title="Complete your patient profile"
        description="You need a patient profile before booking or tracking appointments."
        ctaLabel="Create my profile"
        ctaTo={ROUTES.patientProfile}
      />
    );
  }

  const myAppointments = (appointments.data || []).filter((appointment) => appointment.patientId === patientProfile.data.id);
  const upcoming = myAppointments.filter(isUpcomingAppointment);

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Patient workspace"
        title={`Welcome back, ${patientProfile.data.name}`}
        description="Track your upcoming visits and manage your care schedule."
      />
      <div className="grid gap-4 md:grid-cols-3">
        <StatCard label="Upcoming appointments" value={upcoming.length} icon={CalendarDays} />
        <StatCard label="Doctors available" value={doctors.data?.length || 0} icon={Stethoscope} />
        <StatCard label="Total history" value={myAppointments.length} icon={ClipboardList} />
      </div>
    </div>
  );
}
