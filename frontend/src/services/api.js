import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle 401 Unauthorized
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('user_email');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  login: async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    return response.data;
  },
  register: async (name, email, password, role = 'VERIFIER') => {
    const response = await api.post('/auth/register', { name, email, password, role });
    return response.data;
  },
};

export const evidenceAPI = {
  upload: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/evidence/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
  verify: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/evidence/verify', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
  getUserEvidence: async () => {
    const response = await api.get('/evidence');
    return response.data;
  },
  getEvidenceDetail: async (evidenceId) => {
    const response = await api.get(`/evidence/${evidenceId}`);
    return response.data;
  },
  getAuditLogs: async (evidenceId) => {
    const response = await api.get(`/evidence/${evidenceId}/audit-logs`);
    return response.data;
  },
  getBlockchainRecord: async (evidenceId) => {
    const response = await api.get(`/evidence/${evidenceId}/blockchain`);
    return response.data;
  },
};

export default api;
