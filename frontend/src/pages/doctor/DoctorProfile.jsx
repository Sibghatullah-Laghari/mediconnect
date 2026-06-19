import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import LoadingSpinner from '../../components/common/LoadingSpinner.jsx';
import PageHeader from '../../components/common/PageHeader.jsx';
import Button from '../../components/ui/Button.jsx';
import { Card, CardContent } from '../../components/ui/Card.jsx';
import Input from '../../components/ui/Input.jsx';
import Select from '../../components/ui/Select.jsx';
import { useAuth } from '../../auth/useAuth.js';
import { useDoctorProfile, useDoctorProfileMutation } from '../../hooks/useDoctors.js';
import { useToast } from '../../hooks/useToast.jsx';
import { getErrorMessage } from '../../utils/errorUtils.js';

const schema = z.object({
  name: z.string().min(3, 'Full name is required'),
  gender: z.enum(['MALE', 'FEMALE', 'OTHER']),
  specialization: z.string().min(2, 'Specialization is required'),
  phone: z.string().regex(/^[0-9]{10,15}$/, 'Phone must contain 10 to 15 digits'),
  email: z.string().email(),
  fee: z.coerce.number().nonnegative('Fee must be zero or more'),
  experience: z.coerce.number().nonnegative('Experience must be zero or more'),
});

export default function DoctorProfile() {
  const { user } = useAuth();
  const toast = useToast();
  const profileQuery = useDoctorProfile();
  const mutation = useDoctorProfileMutation();
  const {
    register,
    reset,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name: '',
      gender: 'OTHER',
      specialization: '',
      phone: '',
      email: user?.email || '',
      fee: 0,
      experience: 0,
    },
  });

  useEffect(() => {
    if (profileQuery.data) {
      reset(profileQuery.data);
    } else if (user?.email) {
      reset({
        name: '',
        gender: 'OTHER',
        specialization: '',
        phone: '',
        email: user.email,
        fee: 0,
        experience: 0,
      });
    }
  }, [profileQuery.data, reset, user?.email]);

  if (profileQuery.isLoading) {
    return <LoadingSpinner label="Loading doctor profile" />;
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Doctor profile"
        title="Manage your professional profile"
        description="Keep your contact details, fee, and specialization accurate."
      />
      <Card>
        <CardContent className="p-6">
          <form
            className="grid gap-4 md:grid-cols-2"
            onSubmit={handleSubmit(async (values) => {
              try {
                await mutation.mutateAsync({
                  id: profileQuery.data?.id,
                  payload: values,
                });
                toast.success('Doctor profile saved successfully.');
              } catch (error) {
                toast.error(getErrorMessage(error, 'Unable to save doctor profile.'));
              }
            })}
          >
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Full name</label>
              <Input {...register('name')} />
              {errors.name ? <p className="mt-1 text-xs text-red-600">{errors.name.message}</p> : null}
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Gender</label>
              <Select {...register('gender')}>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </Select>
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Specialization</label>
              <Input {...register('specialization')} />
              {errors.specialization ? <p className="mt-1 text-xs text-red-600">{errors.specialization.message}</p> : null}
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Phone</label>
              <Input {...register('phone')} />
              {errors.phone ? <p className="mt-1 text-xs text-red-600">{errors.phone.message}</p> : null}
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Email</label>
              <Input {...register('email')} readOnly />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Consultation fee</label>
              <Input type="number" step="0.01" {...register('fee')} />
              {errors.fee ? <p className="mt-1 text-xs text-red-600">{errors.fee.message}</p> : null}
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Years of experience</label>
              <Input type="number" {...register('experience')} />
              {errors.experience ? <p className="mt-1 text-xs text-red-600">{errors.experience.message}</p> : null}
            </div>
            <div className="md:col-span-2">
              <Button type="submit" loading={isSubmitting || mutation.isPending}>
                Save doctor profile
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
