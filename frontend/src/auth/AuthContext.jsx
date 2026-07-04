import { createContext, useEffect, useMemo, useState } from 'react';
import { getCurrentUser, loginUser, registerUser } from '../api/auth.api.js';
import { registerUnauthorizedHandler } from '../api/axios.js';
import { clearTokens, getRefreshToken, setTokens } from './tokenManager.js';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    registerUnauthorizedHandler(() => {
      clearTokens();
      setUser(null);
    });
  }, []);

  useEffect(() => {
    let active = true;

    async function restoreSession() {
      if (!getRefreshToken()) {
        if (active) setLoading(false);
        return;
      }

      try {
        const me = await getCurrentUser();
        if (active) {
          setUser(me);
        }
      } catch {
        clearTokens();
        if (active) {
          setUser(null);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    restoreSession();
    return () => {
      active = false;
    };
  }, []);

const value = useMemo(
  () => ({
    user,
    loading,
    isAuthenticated: Boolean(user),

    async login(values) {
      const response = await loginUser(values);
      setTokens(response.token, response.refreshToken);
      const me = await getCurrentUser();
      setUser(me);
      return me;
    },

    async register(values) {
      return registerUser(values);
    },

    logout() {
      clearTokens();
      setUser(null);
    },

    async refreshUser() {
      const me = await getCurrentUser();
      setUser(me);
      return me;
    },

    loginWithGoogle() {
      throw new Error('Google authentication is not configured for this environment.');
    },
  }),
  [loading, user]
);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
