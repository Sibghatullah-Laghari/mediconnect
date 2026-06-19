import AppointmentTable from '../../components/appointments/AppointmentTable.jsx';
import EmptyState from '../../components/common/EmptyState.jsx';
import LoadingSpinner from '../../components/common/LoadingSpinner.jsx';
import PageHeader from '../../components/common/PageHeader.jsx';
import { ROUTES } from '../../constants/routes.js';
import { useAppointments, useAppointmentStatusMutation } from '../../hooks/useAppointments.js';
import { usePatientProfile } from '../../hooks/usePatients.js';
import { useToast } from '../../hooks/useToast.jsx';
import { getErrorMessage } from '../../utils/errorUtils.js';

export default function MyAppointments() {
  const toast = useToast();
  const patientProfile = usePatientProfile();
  const appointments = useAppointments(patientProfile.isSuccess);
  const statusMutation = useAppointmentStatusMutation();

  if (patientProfile.isLoading || appointments.isLoading) {
    return <LoadingSpinner label="Loading your appointments" />;
  }

  if (!patientProfile.data) {
    return (
      <EmptyState
        title="Patient profile required"
        description="Create your patient profile before viewing appointments."
        ctaLabel="Create patient profile"
        ctaTo={ROUTES.patientProfile}
      />
    );
  }

  const rows = (appointments.data || []).filter((appointment) => appointment.patientId === patientProfile.data.id);

  if (!rows.length) {
    return (
      <EmptyState
        title="No appointments yet"
        description="You have not booked any appointments yet."
        ctaLabel="Book your first appointment"
        ctaTo={ROUTES.patientBook}
      />
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Patient appointments"
        title="My appointments"
        description="Track upcoming appointments and cancel when plans change."
      />
      <AppointmentTable
        appointments={rows}
        role="PATIENT"
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
