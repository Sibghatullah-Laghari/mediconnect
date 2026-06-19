import { useState } from 'react';
import BookingForm from '../../components/appointments/BookingForm.jsx';
import EmptyState from '../../components/common/EmptyState.jsx';
import LoadingSpinner from '../../components/common/LoadingSpinner.jsx';
import PageHeader from '../../components/common/PageHeader.jsx';
import { Card, CardContent } from '../../components/ui/Card.jsx';
import { ROUTES } from '../../constants/routes.js';
import { useAppointmentMutation } from '../../hooks/useAppointments.js';
import { useDoctors, useDoctorSpecializations } from '../../hooks/useDoctors.js';
import { usePatientProfile } from '../../hooks/usePatients.js';
import { useToast } from '../../hooks/useToast.jsx';
import { formatAppointmentDate } from '../../utils/dateUtils.js';
import { getErrorMessage } from '../../utils/errorUtils.js';

export default function BookAppointment() {
  const toast = useToast();
  const patientProfile = usePatientProfile();
  const doctors = useDoctors();
  const specializations = useDoctorSpecializations();
  const appointmentMutation = useAppointmentMutation();
  const [confirmation, setConfirmation] = useState(null);

  if (patientProfile.isLoading || doctors.isLoading || specializations.isLoading) {
    return <LoadingSpinner label="Preparing booking flow" />;
  }

  if (!patientProfile.data) {
    return (
      <EmptyState
        title="Create your patient profile first"
        description="Appointment booking is available once your patient details are saved."
        ctaLabel="Go to patient profile"
        ctaTo={ROUTES.patientProfile}
      />
    );
  }

  if (confirmation) {
    return (
      <Card>
        <CardContent className="p-6">
          <PageHeader
            eyebrow="Appointment booked"
            title="Your appointment is confirmed in the system"
            description={`${confirmation.doctorName} on ${formatAppointmentDate(
              confirmation.appointmentDate,
              confirmation.appointmentTime
            )}`}
          />
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Appointment flow"
        title="Book a new appointment"
        description="Choose a specialization, select a doctor, then confirm your preferred slot."
      />
      <BookingForm
        doctors={doctors.data || []}
        specializations={specializations.data || []}
        patientId={patientProfile.data.id}
        loading={appointmentMutation.isPending}
        onSubmit={async (payload) => {
          try {
            const response = await appointmentMutation.mutateAsync({ payload });
            setConfirmation(response);
            toast.success('Appointment booked successfully.');
          } catch (error) {
            toast.error(getErrorMessage(error, 'Unable to book appointment.'));
          }
        }}
      />
    </div>
  );
}
