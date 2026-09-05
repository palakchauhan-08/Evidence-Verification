import React, { createContext, useContext, useState, useEffect, useMemo } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

const parseJwt = (token) => {
  if (!token) return null;
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
};

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('jwt_token') || null);
  const [userEmail, setUserEmail] = useState(localStorage.getItem('user_email') || null);
  const [loading, setLoading] = useState(false);

  const decodedJwt = useMemo(() => parseJwt(token), [token]);
  const userRole = useMemo(() => {
    if (!token) return null;
    const roleFromJwt = decodedJwt?.role;
    if (roleFromJwt) {
      return roleFromJwt.toUpperCase();
    }
    return 'INVESTIGATOR';
  }, [token, decodedJwt]);

  useEffect(() => {
    if (token) {
      localStorage.setItem('jwt_token', token);
      if (!userEmail && decodedJwt?.sub) {
        setUserEmail(decodedJwt.sub);
      }
    } else {
      localStorage.removeItem('jwt_token');
    }
  }, [token, userEmail, decodedJwt]);

  useEffect(() => {
    if (userEmail) {
      localStorage.setItem('user_email', userEmail);
    } else {
      localStorage.removeItem('user_email');
    }
  }, [userEmail]);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const data = await authAPI.login(email, password);
      if (data && data.token) {
        setToken(data.token);
        setUserEmail(email);
        return { success: true, message: data.message || 'Login successful' };
      } else {
        return { success: false, message: 'Invalid response from server' };
      }
    } catch (err) {
      const errMsg = err.response?.data?.error || err.response?.data?.message || err.message || 'Login failed';
      return { success: false, message: errMsg };
    } finally {
      setLoading(false);
    }
  };

  const register = async (name, email, password, role = 'INVESTIGATOR') => {
    setLoading(true);
    try {
      const data = await authAPI.register(name, email, password, role);
      if (typeof data === 'string' && data.toLowerCase().includes('already')) {
        return { success: false, message: data };
      }
      return { success: true, message: typeof data === 'string' ? data : 'User registered successfully' };
    } catch (err) {
      const errMsg = err.response?.data?.error || err.response?.data?.message || err.message || 'Registration failed';
      return { success: false, message: errMsg };
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setToken(null);
    setUserEmail(null);
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_email');
  };

  const hasRole = (role) => {
    return userRole === role;
  };

  const hasAnyRole = (roles) => {
    if (!Array.isArray(roles)) return false;
    return roles.includes(userRole);
  };

  const value = {
    token,
    userEmail,
    userRole,
    isAuthenticated: !!token,
    loading,
    login,
    register,
    logout,
    hasRole,
    hasAnyRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
