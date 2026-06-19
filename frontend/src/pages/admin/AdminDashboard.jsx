import { CalendarDays, ClipboardList, Stethoscope, Users } from 'lucide-react';
import LoadingSpinner from '../../components/common/LoadingSpinner.jsx';
import PageHeader from '../../components/common/PageHeader.jsx';
import StatCard from '../../components/common/StatCard.jsx';
import { useAppointments } from '../../hooks/useAppointments.js';
import { useDoctors } from '../../hooks/useDoctors.js';
import { usePatients } from '../../hooks/usePatients.js';

export default function AdminDashboard() {
  const patients = usePatients();
  const doctors = useDoctors();
  const appointments = useAppointments();

  if (patients.isLoading || doctors.isLoading || appointments.isLoading) {
    return <LoadingSpinner label="Loading admin overview" />;
  }

  const today = new Date().toISOString().slice(0, 10);
  const todaysAppointments = (appointments.data || []).filter((appointment) => appointment.appointmentDate === today);

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Admin overview"
        title="Operations dashboard"
        description="Monitor platform-wide throughput across patients, doctors, and appointments."
      />
      <div className="grid gap-4 md:grid-cols-4">
        <StatCard label="Patients" value={patients.data?.length || 0} icon={Users} />
        <StatCard label="Doctors" value={doctors.data?.length || 0} icon={Stethoscope} />
        <StatCard label="Appointments" value={appointments.data?.length || 0} icon={ClipboardList} />
        <StatCard label="Today" value={todaysAppointments.length} icon={CalendarDays} />
      </div>
    </div>
  );
}
