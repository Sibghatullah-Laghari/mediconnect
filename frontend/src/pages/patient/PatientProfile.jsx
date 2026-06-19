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
import { usePatientProfile, usePatientProfileMutation } from '../../hooks/usePatients.js';
import { useToast } from '../../hooks/useToast.jsx';
import { getErrorMessage } from '../../utils/errorUtils.js';

const schema = z.object({
  name: z.string().min(3, 'Full name is required'),
  email: z.string().email(),
  phone: z.string().regex(/^[0-9]{10,15}$/, 'Phone must contain 10 to 15 digits'),
  dateOfBirth: z.string().refine((value) => new Date(value) <= new Date(), 'Date of birth cannot be in the future'),
  gender: z.enum(['MALE', 'FEMALE', 'OTHER']),
  address: z.string().min(5, 'Address is required'),
});

export default function PatientProfile() {
  const { user } = useAuth();
  const toast = useToast();
  const profileQuery = usePatientProfile();
  const mutation = usePatientProfileMutation();
  const {
    register,
    reset,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name: '',
      email: user?.email || '',
      phone: '',
      dateOfBirth: '',
      gender: 'OTHER',
      address: '',
    },
  });

  useEffect(() => {
    if (profileQuery.data) {
      reset({
        name: profileQuery.data.name,
        email: profileQuery.data.email,
        phone: profileQuery.data.phone,
        dateOfBirth: profileQuery.data.dateOfBirth,
        gender: profileQuery.data.gender,
        address: profileQuery.data.address || '',
      });
    } else if (user?.email) {
      reset({
        name: '',
        email: user.email,
        phone: '',
        dateOfBirth: '',
        gender: 'OTHER',
        address: '',
      });
    }
  }, [profileQuery.data, reset, user?.email]);

  if (profileQuery.isLoading) {
    return <LoadingSpinner label="Loading patient profile" />;
  }

  return (
    <div className="space-y-6">
      <PageHeader
        eyebrow="Patient profile"
        title="Manage your patient details"
        description="Keep your contact and demographic information accurate for scheduling."
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
                toast.success('Patient profile saved successfully.');
              } catch (error) {
                toast.error(getErrorMessage(error, 'Unable to save patient profile.'));
              }
            })}
          >
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Full name</label>
              <Input {...register('name')} />
              {errors.name ? <p className="mt-1 text-xs text-red-600">{errors.name.message}</p> : null}
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Email</label>
              <Input {...register('email')} readOnly />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Phone</label>
              <Input {...register('phone')} />
              {errors.phone ? <p className="mt-1 text-xs text-red-600">{errors.phone.message}</p> : null}
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Date of birth</label>
              <Input type="date" {...register('dateOfBirth')} />
              {errors.dateOfBirth ? <p className="mt-1 text-xs text-red-600">{errors.dateOfBirth.message}</p> : null}
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Gender</label>
              <Select {...register('gender')}>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </Select>
            </div>
            <div className="md:col-span-2">
              <label className="mb-1 block text-sm font-medium text-slate-700">Address</label>
              <textarea
                {...register('address')}
                rows={4}
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-4 focus:ring-primary/10"
              />
              {errors.address ? <p className="mt-1 text-xs text-red-600">{errors.address.message}</p> : null}
            </div>
            <div className="md:col-span-2">
              <Button type="submit" loading={isSubmitting || mutation.isPending}>
                Save patient profile
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
