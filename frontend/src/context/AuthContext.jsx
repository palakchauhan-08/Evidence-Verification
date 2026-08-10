import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('jwt_token') || null);
  const [userEmail, setUserEmail] = useState(localStorage.getItem('user_email') || null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (token) {
      localStorage.setItem('jwt_token', token);
    } else {
      localStorage.removeItem('jwt_token');
    }
  }, [token]);

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

  const register = async (name, email, password, role = 'VERIFIER') => {
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

  const value = {
    token,
    userEmail,
    isAuthenticated: !!token,
    loading,
    login,
    register,
    logout,
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
