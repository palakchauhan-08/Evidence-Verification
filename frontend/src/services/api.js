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
  register: async (name, email, password, role = 'INVESTIGATOR') => {
    const response = await api.post('/auth/register', { name, email, password, role });
    return response.data;
  },
  verifyEmail: async (token) => {
    const response = await api.get(`/auth/verify-email?token=${encodeURIComponent(token)}`);
    return response.data;
  },
  resendVerification: async (email) => {
    const response = await api.post('/auth/resend-verification', { email });
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
  startReview: async (evidenceId) => {
    const response = await api.post(`/evidence/${evidenceId}/review/start`);
    return response.data;
  },
  reject: async (evidenceId, reason) => {
    const response = await api.post(`/evidence/${evidenceId}/reject`, { reason });
    return response.data;
  },
  getChainOfCustody: async (evidenceId) => {
    const response = await api.get(`/evidence/${evidenceId}/chain-of-custody`);
    return response.data;
  },
  getStatusHistory: async (evidenceId) => {
    const response = await api.get(`/evidence/${evidenceId}/status-history`);
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
  downloadVerificationReport: async (evidenceId) => {
    const response = await api.get(`/evidence/${evidenceId}/verification-report`, {
      responseType: 'blob',
    });
    return response.data;
  },
  downloadEvidenceQrCode: async (evidenceId) => {
    const response = await api.get(`/evidence/${evidenceId}/qr`, {
      responseType: 'blob',
    });
    return response.data;
  },
  getNotes: async (evidenceId) => {
    const response = await api.get(`/evidence/${evidenceId}/notes`);
    return response.data;
  },
  addNote: async (evidenceId, content) => {
    const response = await api.post(`/evidence/${evidenceId}/notes`, { content });
    return response.data;
  },
  updateNote: async (evidenceId, noteId, content) => {
    const response = await api.put(`/evidence/${evidenceId}/notes/${noteId}`, { content });
    return response.data;
  },
  deleteNote: async (evidenceId, noteId) => {
    const response = await api.delete(`/evidence/${evidenceId}/notes/${noteId}`);
    return response.data;
  },
  getVersions: async (evidenceId) => {
    const response = await api.get(`/evidence/${evidenceId}/versions`);
    return response.data;
  },
  uploadNewVersion: async (evidenceId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post(`/evidence/${evidenceId}/versions`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },
  downloadVersionVerificationReport: async (evidenceId, versionNumber) => {
    const response = await api.get(`/evidence/${evidenceId}/versions/${versionNumber}/verification-report`, {
      responseType: 'blob',
    });
    return response.data;
  },
};

export const caseAPI = {
  createCase: async (title, description, priority, assignedInvestigator) => {
    const response = await api.post('/cases', { title, description, priority, assignedInvestigator });
    return response.data;
  },
  getCases: async (params = {}) => {
    const query = new URLSearchParams(params).toString();
    const url = query ? `/cases?${query}` : '/cases';
    const response = await api.get(url);
    return response.data;
  },
  getCaseDetail: async (caseId) => {
    const response = await api.get(`/cases/${caseId}`);
    return response.data;
  },
  updateStatus: async (caseId, status, reason) => {
    const response = await api.patch(`/cases/${caseId}/status`, { status, reason });
    return response.data;
  },
  assignInvestigator: async (caseId, assignedInvestigator) => {
    const response = await api.patch(`/cases/${caseId}/assign`, { assignedInvestigator });
    return response.data;
  },
  addEvidence: async (caseId, evidenceId) => {
    const response = await api.post(`/cases/${caseId}/evidence/${evidenceId}`);
    return response.data;
  },
  removeEvidence: async (caseId, evidenceId) => {
    const response = await api.delete(`/cases/${caseId}/evidence/${evidenceId}`);
    return response.data;
  },
};

export const adminAPI = {
  getUsers: async () => {
    const response = await api.get('/admin/users');
    return response.data;
  },
  updateUserRole: async (id, role) => {
    const response = await api.put(`/admin/users/${id}/role`, { role });
    return response.data;
  },
};

export const publicAPI = {
  verifyEvidence: async (evidenceId) => {
    const response = await api.get(`/public/verify/evidence/${evidenceId}`);
    return response.data;
  },
};

export default api;
