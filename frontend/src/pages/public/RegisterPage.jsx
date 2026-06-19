import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/useAuth.js';
import Button from '../../components/ui/Button.jsx';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card.jsx';
import Input from '../../components/ui/Input.jsx';
import Select from '../../components/ui/Select.jsx';
import { getDashboardRoute } from '../../constants/routes.js';
import { useToast } from '../../hooks/useToast.jsx';
import { getErrorMessage } from '../../utils/errorUtils.js';

const schema = z.object({
  name: z.string().min(3, 'Full name is required'),
  email: z.string().email('Enter a valid email address'),
  password: z
    .string()
    .min(8, 'Password must be at least 8 characters')
    .regex(/\d/, 'Password must include at least one number'),
  role: z.enum(['PATIENT', 'DOCTOR']),
});

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register: registerAccount } = useAuth();
  const toast = useToast();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      role: 'PATIENT',
    },
  });

  return (
    <div className="mx-auto max-w-md">
      <Card>
        <CardHeader>
          <CardTitle>Create your MediConnect account</CardTitle>
        </CardHeader>
        <CardContent>
          <form
            className="space-y-4"
            onSubmit={handleSubmit(async (values) => {
              try {
                const user = await registerAccount(values);
                toast.success('Account created successfully.');
                navigate(getDashboardRoute(user.role), { replace: true });
              } catch (error) {
                toast.error(getErrorMessage(error, 'Unable to register account.'));
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
              <Input type="email" {...register('email')} />
              {errors.email ? <p className="mt-1 text-xs text-red-600">{errors.email.message}</p> : null}
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Password</label>
              <Input type="password" {...register('password')} />
              {errors.password ? <p className="mt-1 text-xs text-red-600">{errors.password.message}</p> : null}
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-slate-700">Role</label>
              <Select {...register('role')}>
                <option value="PATIENT">Patient</option>
                <option value="DOCTOR">Doctor</option>
              </Select>
            </div>
            <Button type="submit" loading={isSubmitting} fullWidth>
              Create account
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
