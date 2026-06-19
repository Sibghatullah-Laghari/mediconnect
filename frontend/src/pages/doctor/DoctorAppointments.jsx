import AppointmentTable from '../../components/appointments/AppointmentTable.jsx';
import EmptyState from '../../components/common/EmptyState.jsx';
import LoadingSpinner from '../../components/common/LoadingSpinner.jsx';
import PageHeader from '../../components/common/PageHeader.jsx';
import { ROUTES } from '../../constants/routes.js';
import { useAppointments, useAppointmentStatusMutation } from '../../hooks/useAppointments.js';
import { useDoctorProfile } from '../../hooks/useDoctors.js';
import { useToast } from '../../hooks/useToast.jsx';
import { getErrorMessage } from '../../utils/errorUtils.js';

export default function DoctorAppointments() {
  const profile = useDoctorProfile();
  const appointments = useAppointments(profile.isSuccess);
  const statusMutation = useAppointmentStatusMutation();
  const toast = useToast();

  if (profile.isLoading || appointments.isLoading) {
    return <LoadingSpinner label="Loading doctor appointments" />;
  }

  if (!profile.data) {
    return (
      <EmptyState
        title="Doctor profile required"
        description="Create your doctor profile before managing appointments."
        ctaLabel="Complete doctor profile"
        ctaTo={ROUTES.doctorProfile}
      />
    );
  }

  const rows = (appointments.data || []).filter((appointment) => appointment.doctorId === profile.data.id);

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Doctor appointments"
        title="Manage assigned appointments"
        description="Confirm new appointments and complete them after each consultation."
      />
      {!rows.length ? (
        <EmptyState title="No appointments assigned" description="Your schedule is currently clear." />
      ) : (
        <AppointmentTable
          appointments={rows}
          role="DOCTOR"
          onAction={async (action, appointment) => {
            try {
              await statusMutation.mutateAsync({ id: appointment.id, action });
              toast.success('Appointment status updated.');
            } catch (error) {
              toast.error(getErrorMessage(error, 'Unable to update appointment status.'));
            }
          }}
        />
      )}
    </div>
  );
}
