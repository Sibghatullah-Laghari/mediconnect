import { Navigate, Outlet, useLocation } from 'react-router-dom';
import LoadingSpinner from '../components/common/LoadingSpinner.jsx';
import { getDashboardRoute, ROUTES } from '../constants/routes.js';
import { useAuth } from './useAuth.js';

export default function ProtectedRoute({ roles }) {
  const { isAuthenticated, loading, user } = useAuth();
  const location = useLocation();

  if (loading) {
    return <LoadingSpinner fullscreen label="Restoring secure session" />;
  }

  if (!isAuthenticated) {
    return <Navigate replace to={ROUTES.login} state={{ from: location }} />;
  }

  if (roles?.length && !roles.includes(user.role)) {
    return <Navigate replace to={getDashboardRoute(user.role)} />;
  }

  return <Outlet />;
}
