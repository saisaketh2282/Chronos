import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  validateToken: (data) => api.post('/auth/validate', data),
  getCurrentUser: () => api.get('/auth/me'),
};

// Jobs API
export const jobsAPI = {
  getJobs: (params = {}) => api.get('/jobs', { params }),
  getJob: (id, includeLogs = false) => api.get(`/jobs/${id}`, { 
    params: { includeLogs } 
  }),
  createJob: (jobData) => api.post('/jobs', jobData),
  updateJob: (id, jobData) => api.put(`/jobs/${id}`, jobData),
  deleteJob: (id) => api.delete(`/jobs/${id}`),
  cancelJob: (id) => api.post(`/jobs/${id}/cancel`),
  getJobLogs: (id, params = {}) => api.get(`/jobs/${id}/logs`, { params }),
  getJobStatistics: () => api.get('/jobs/statistics'),
  getJobsByStatus: (status) => api.get(`/jobs/status/${status}`),
};

// Admin API
export const adminAPI = {
  getStuckJobs: (timeoutMinutes = 30) => api.get('/admin/jobs/stuck', {
    params: { timeoutMinutes }
  }),
  resetStuckJobs: (timeoutMinutes = 30) => api.post('/admin/jobs/stuck/reset', null, {
    params: { timeoutMinutes }
  }),
  getJobsForCleanup: (daysOld = 30) => api.get('/admin/jobs/cleanup', {
    params: { daysOld }
  }),
  cleanupOldJobs: (daysOld = 30) => api.delete('/admin/jobs/cleanup', {
    params: { daysOld }
  }),
  getSystemStatistics: () => api.get('/admin/statistics'),
};

// Health API
export const healthAPI = {
  getHealth: () => api.get('/health'),
  getReadiness: () => api.get('/health/ready'),
  getLiveness: () => api.get('/health/live'),
};

export default api;
