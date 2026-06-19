import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { useLocation, useNavigate } from 'react-router-dom';
import Button from '../../components/ui/Button.jsx';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card.jsx';
import Input from '../../components/ui/Input.jsx';
import { getDashboardRoute } from '../../constants/routes.js';
import { useAuth } from '../../auth/useAuth.js';
import { getErrorMessage } from '../../utils/errorUtils.js';
import { useToast } from '../../hooks/useToast.jsx';

const schema = z.object({
  email: z.string().email('Enter a valid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
});

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const toast = useToast();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
  });

  return (
    <div className="mx-auto max-w-md">
      <Card>
        <CardHeader>
          <CardTitle>Secure sign in</CardTitle>
        </CardHeader>
        <CardContent>
          <form
            className="space-y-4"
            onSubmit={handleSubmit(async (values) => {
              try {
                const user = await login(values);
                toast.success('Signed in successfully.');
                navigate(location.state?.from?.pathname || getDashboardRoute(user.role), { replace: true });
              } catch (error) {
                toast.error(getErrorMessage(error, 'Unable to sign in.'));
              }
            })}
          >
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
            <Button type="submit" loading={isSubmitting} fullWidth>
              Sign in
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
