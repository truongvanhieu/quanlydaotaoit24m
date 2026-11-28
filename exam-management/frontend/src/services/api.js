import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

const client = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Response interceptor for clear error extraction
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ||
      error.message ||
      'Đã xảy ra lỗi khi kết nối tới máy chủ!';
    return Promise.reject(new Error(message));
  }
);

export const examTypesApi = {
  getAll: (params) => client.get('/exam-types', { params }),
  getById: (id) => client.get(`/exam-types/${id}`),
  create: (data) => client.post('/exam-types', data),
  update: (id, data) => client.put(`/exam-types/${id}`, data),
  delete: (id) => client.delete(`/exam-types/${id}`),
};

export const examsApi = {
  getAll: (params) => client.get('/exams', { params }),
  getById: (id) => client.get(`/exams/${id}`),
  create: (data) => client.post('/exams', data),
  update: (id, data) => client.put(`/exams/${id}`, data),
  delete: (id) => client.delete(`/exams/${id}`),
};

export const invigilatorsApi = {
  getAll: () => client.get('/invigilators'),
  create: (data) => client.post('/invigilators', data),
};

export const examRoomsApi = {
  getAll: (params) => client.get('/exam-rooms', { params }),
  getById: (id) => client.get(`/exam-rooms/${id}`),
  create: (data) => client.post('/exam-rooms', data),
  update: (id, data) => client.put(`/exam-rooms/${id}`, data),
  delete: (id) => client.delete(`/exam-rooms/${id}`),
};

export const examRegistrationsApi = {
  transferRooms: (id) => client.get(`/exam-registrations/${id}/transfer-rooms`),
  getGroups: (params) => client.get('/exam-registrations/groups', { params }),
  classifyGroup: (data) => client.post('/exam-registrations/classify-group', data),
  bulkCreate: (data) => client.post('/exam-registrations/bulk', data),
  getAll: (params) => client.get('/exam-registrations', { params }),
  getById: (id) => client.get(`/exam-registrations/${id}`),
  create: (data) => client.post('/exam-registrations', data),
  update: (id, data) => client.put(`/exam-registrations/${id}`, data),
  delete: (id) => client.delete(`/exam-registrations/${id}`),
};

export const examResultsApi = {
  getAll: (params) => client.get('/exam-results', { params }),
  getById: (id) => client.get(`/exam-results/${id}`),
  create: (data) => client.post('/exam-results', data),
  update: (id, data) => client.put(`/exam-results/${id}`, data),
  toggleLock: (id, is_locked) => client.patch(`/exam-results/${id}/toggle-lock`, { is_locked }),
  updateAppeal: (id, data) => client.patch(`/exam-results/${id}/appeal`, data),
  delete: (id) => client.delete(`/exam-results/${id}`),
};

export const examPapersApi = {
  getAll: (params) => client.get('/exam-papers', { params }),
  getById: (id) => client.get(`/exam-papers/${id}`),
  create: (data) => client.post('/exam-papers', data),
  update: (id, data) => client.put(`/exam-papers/${id}`, data),
  delete: (id) => client.delete(`/exam-papers/${id}`),
};

export const lookupsApi = {
  getAll: () => client.get('/lookups/all'),
  create: (kind, data) => client.post(`/master-data/${kind}`, data),
};

export const studentPortalApi = {
  getByStudentCode: (code) => client.get(`/student-portal/${code}`),
};

export default client;
