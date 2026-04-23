import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  startTransition,
} from "react";
import { apiRequest } from "../lib/api.js";

const AuthContext = createContext(null);
const AUTH_STORAGE_KEY = "inkwell_auth";

function readStoredAuth() {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) {
      return { token: null, user: null };
    }
    const data = JSON.parse(raw);
    return { token: data.token ?? null, user: data.user ?? null };
  } catch {
    return { token: null, user: null };
  }
}

export function AuthProvider({ children }) {
  const [{ token, user }, setAuth] = useState(readStoredAuth);

  useEffect(() => {
    if (!token || !user) {
      localStorage.removeItem(AUTH_STORAGE_KEY);
      return;
    }
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify({ token, user }));
  }, [token, user]);

  const login = async (credentials) => {
    const result = await apiRequest("/auth/login", {
      method: "POST",
      body: JSON.stringify(credentials),
    });
    startTransition(() => {
      setAuth({ token: result.accessToken, user: result.user });
    });
    return result;
  };

  const signup = async (payload) => {
    const result = await apiRequest("/auth/register", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    startTransition(() => {
      setAuth({ token: result.accessToken, user: result.user });
    });
    return result;
  };

  const logout = () => {
    startTransition(() => {
      setAuth({ token: null, user: null });
    });
  };

  const updateUser = (updates) => {
    startTransition(() => {
      setAuth((current) => ({
        token: current.token,
        user: { ...current.user, ...updates },
      }));
    });
  };

  const value = useMemo(
    () => ({
      token,
      user,
      role: user?.role || null,
      isAuthenticated: Boolean(token && user),
      login,
      signup,
      logout,
      updateUser,
    }),
    [token, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return value;
}
