import { Navigate, Outlet, useLocation } from 'react-router-dom';
import LoadingSpinner from '../components/common/LoadingSpinner.jsx';
import { getDashboardRoute, ROUTES } from '../constants/routes.js';
import { useAuth } from './useAuth.js';

/**
 * ProtectedRoute — guards routes that require authentication and optional role-based access.
 * Renders child routes (Outlet) only if user is authenticated and has required role.
 * Otherwise redirects to login or appropriate dashboard.
 */
export default function ProtectedRoute({ roles }) {
  // Get auth state from context/hook
  const { isAuthenticated, loading, user } = useAuth();
  const location = useLocation(); // For redirecting back after login

  // Show loader while session is being restored (e.g., token validation)
  if (loading) {
    return <LoadingSpinner fullscreen label="Restoring secure session" />;
  }

  // Not authenticated → redirect to login, preserving the attempted location
  if (!isAuthenticated) {
    return <Navigate replace to={ROUTES.login} state={{ from: location }} />;
  }

  // Authenticated but role not authorized → redirect to user's own dashboard
  if (roles?.length && !roles.includes(user.role)) {
    return <Navigate replace to={getDashboardRoute(user.role)} />;
  }

  // All checks passed → render the nested routes (Outlet)
  return <Outlet />;
}
