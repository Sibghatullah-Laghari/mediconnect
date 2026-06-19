import AppointmentTable from '../../components/appointments/AppointmentTable.jsx';
import EmptyState from '../../components/common/EmptyState.jsx';
import LoadingSpinner from '../../components/common/LoadingSpinner.jsx';
import PageHeader from '../../components/common/PageHeader.jsx';
import { useAppointments, useAppointmentStatusMutation } from '../../hooks/useAppointments.js';
import { useToast } from '../../hooks/useToast.jsx';
import { getErrorMessage } from '../../utils/errorUtils.js';

export default function AppointmentsManagement() {
  const appointments = useAppointments();
  const statusMutation = useAppointmentStatusMutation();
  const toast = useToast();

  if (appointments.isLoading) {
    return <LoadingSpinner label="Loading appointments" />;
  }

  if (!appointments.data?.length) {
    return <EmptyState title="No appointments found" description="No appointment records are available yet." />;
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Admin appointments"
        title="Manage all appointments"
        description="Review status, cancel appointments, or remove records."
      />
      <AppointmentTable
        appointments={appointments.data}
        role="ADMIN"
        onAction={async (action, appointment) => {
          try {
            await statusMutation.mutateAsync({ id: appointment.id, action });
            toast.success('Appointment updated successfully.');
          } catch (error) {
            toast.error(getErrorMessage(error, 'Unable to update appointment.'));
          }
        }}
      />
    </div>
  );
}
