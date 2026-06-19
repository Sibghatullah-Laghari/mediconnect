import { CheckCircle2, ClipboardList, Clock3 } from 'lucide-react';
import EmptyState from '../../components/common/EmptyState.jsx';
import LoadingSpinner from '../../components/common/LoadingSpinner.jsx';
import PageHeader from '../../components/common/PageHeader.jsx';
import StatCard from '../../components/common/StatCard.jsx';
import { ROUTES } from '../../constants/routes.js';
import { useAppointments } from '../../hooks/useAppointments.js';
import { useDoctorProfile } from '../../hooks/useDoctors.js';

export default function DoctorDashboard() {
  const profile = useDoctorProfile();
  const appointments = useAppointments(profile.isSuccess);

  if (profile.isLoading || appointments.isLoading) {
    return <LoadingSpinner label="Loading doctor dashboard" />;
  }

  if (!profile.data) {
    return (
      <EmptyState
        title="Complete your doctor profile"
        description="You need a doctor profile before appointments can be assigned to you."
        ctaLabel="Open doctor profile"
        ctaTo={ROUTES.doctorProfile}
      />
    );
  }

  const rows = (appointments.data || []).filter((appointment) => appointment.doctorId === profile.data.id);
  const pending = rows.filter((appointment) => appointment.status === 'PENDING').length;
  const confirmed = rows.filter((appointment) => appointment.status === 'CONFIRMED').length;

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Doctor workspace"
        title={`Dr. ${profile.data.name}`}
        description="Review today’s workload, confirm appointments, and close out completed care."
      />
      <div className="grid gap-4 md:grid-cols-3">
        <StatCard label="Assigned appointments" value={rows.length} icon={ClipboardList} />
        <StatCard label="Awaiting confirmation" value={pending} icon={Clock3} />
        <StatCard label="Confirmed" value={confirmed} icon={CheckCircle2} />
      </div>
    </div>
  );
}
