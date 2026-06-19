import { zodResolver } from '@hookform/resolvers/zod';
import { useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import Button from '../ui/Button.jsx';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/Card.jsx';
import Input from '../ui/Input.jsx';
import Select from '../ui/Select.jsx';
import DoctorSelector from '../doctors/DoctorSelector.jsx';

const TIME_SLOTS = ['09:00', '10:00', '11:00', '14:00', '15:00', '16:00'];

const bookingSchema = z.object({
  specialization: z.string().min(1, 'Choose a specialization'),
  doctorId: z.coerce.number().positive('Choose a doctor'),
  appointmentDate: z
    .string()
    .min(1, 'Choose a date')
    .refine((value) => {
      const selected = new Date(`${value}T00:00:00`);
      const now = new Date();
      now.setHours(0, 0, 0, 0);
      return selected >= now;
    }, 'Appointment date cannot be in the past')
    .refine((value) => new Date(`${value}T00:00:00`).getDay() !== 0, 'Appointments are not available on Sundays'),
  appointmentTime: z.enum(TIME_SLOTS, { errorMap: () => ({ message: 'Choose a time slot' }) }),
  reason: z.string().min(10, 'Reason must be at least 10 characters'),
});

export default function BookingForm({ doctors, specializations, patientId, onSubmit, loading }) {
  const [step, setStep] = useState(1);

  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
    setValue,
    trigger,
  } = useForm({
    resolver: zodResolver(bookingSchema),
    defaultValues: {
      specialization: '',
      doctorId: 0,
      appointmentDate: '',
      appointmentTime: '09:00',
      reason: '',
    },
  });

  const values = watch();
  const filteredDoctors = useMemo(
    () => doctors.filter((doctor) => !values.specialization || doctor.specialization === values.specialization),
    [doctors, values.specialization]
  );

  const selectedDoctor = doctors.find((doctor) => doctor.id === Number(values.doctorId));

  const next = async () => {
    const fieldsByStep = {
      1: ['specialization'],
      2: ['doctorId'],
      3: ['appointmentDate'],
      4: ['appointmentTime'],
      5: ['reason'],
    };

    const fields = fieldsByStep[step];
    if (fields) {
      const valid = await trigger(fields);
      if (!valid) return;
    }

    if (step < 6) {
      setStep((current) => current + 1);
    }
  };

  const previous = () => setStep((current) => Math.max(1, current - 1));

  return (
    <Card>
      <CardHeader>
        <CardTitle>Book a new appointment</CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="flex flex-wrap gap-2">
          {[1, 2, 3, 4, 5, 6].map((value) => (
            <div
              key={value}
              className={`rounded-md px-3 py-1 text-xs font-medium ${
                step === value ? 'bg-primary text-white' : 'bg-slate-100 text-slate-500'
              }`}
            >
              Step {value}
            </div>
          ))}
        </div>

        <form
          onSubmit={handleSubmit((formValues) =>
            onSubmit({
              patientId,
              doctorId: Number(formValues.doctorId),
              appointmentDate: formValues.appointmentDate,
              appointmentTime: formValues.appointmentTime,
              reason: formValues.reason,
            })
          )}
          className="space-y-6"
        >
          {step === 1 ? (
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">Specialization</label>
              <Select {...register('specialization')}>
                <option value="">Select a specialization</option>
                {specializations.map((specialization) => (
                  <option key={specialization} value={specialization}>
                    {specialization}
                  </option>
                ))}
              </Select>
              {errors.specialization ? <p className="mt-1 text-xs text-red-600">{errors.specialization.message}</p> : null}
            </div>
          ) : null}

          {step === 2 ? (
            <div>
              <p className="mb-3 text-sm font-medium text-slate-700">Choose doctor</p>
              <DoctorSelector
                doctors={filteredDoctors}
                value={Number(values.doctorId)}
                onChange={(doctorId) => setValue('doctorId', doctorId, { shouldValidate: true })}
              />
              {errors.doctorId ? <p className="mt-1 text-xs text-red-600">{errors.doctorId.message}</p> : null}
            </div>
          ) : null}

          {step === 3 ? (
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">Choose date</label>
              <Input type="date" {...register('appointmentDate')} />
              {errors.appointmentDate ? <p className="mt-1 text-xs text-red-600">{errors.appointmentDate.message}</p> : null}
            </div>
          ) : null}

          {step === 4 ? (
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">Choose time slot</label>
              <Select {...register('appointmentTime')}>
                {TIME_SLOTS.map((slot) => (
                  <option key={slot} value={slot}>
                    {slot}
                  </option>
                ))}
              </Select>
            </div>
          ) : null}

          {step === 5 ? (
            <div>
              <label className="mb-2 block text-sm font-medium text-slate-700">Reason for visit</label>
              <textarea
                {...register('reason')}
                rows={4}
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-4 focus:ring-primary/10"
                placeholder="Describe the reason for this appointment"
              />
              {errors.reason ? <p className="mt-1 text-xs text-red-600">{errors.reason.message}</p> : null}
            </div>
          ) : null}

          {step === 6 ? (
            <div className="rounded-lg border border-slate-200 bg-slate-50 p-5 text-sm text-slate-700">
              <p>
                <strong>Specialization:</strong> {values.specialization}
              </p>
              <p className="mt-2">
                <strong>Doctor:</strong> {selectedDoctor?.name || 'Not selected'}
              </p>
              <p className="mt-2">
                <strong>Date:</strong> {values.appointmentDate}
              </p>
              <p className="mt-2">
                <strong>Time:</strong> {values.appointmentTime}
              </p>
              <p className="mt-2">
                <strong>Reason:</strong> {values.reason}
              </p>
            </div>
          ) : null}

          <div className="flex justify-between gap-3">
            <Button type="button" variant="outline" onClick={previous} disabled={step === 1 || loading}>
              Back
            </Button>
            {step < 6 ? (
              <Button type="button" onClick={next}>
                Continue
              </Button>
            ) : (
              <Button type="submit" loading={loading}>
                Confirm booking
              </Button>
            )}
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
