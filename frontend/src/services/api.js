import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const uploadCv = async (file, jdId = null) => {
  const formData = new FormData();
  formData.append('file', file);
  if (jdId) {
    formData.append('jdId', jdId);
  }

  const response = await apiClient.post('/cv/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
};

export const getCvStatus = async (trackingId) => {
  const response = await apiClient.get(`/cv/status/${trackingId}`);
  return response.data;
};

export const searchCandidates = async (query, limit = 10) => {
  const response = await apiClient.post('/search', { query, limit });
  return response.data;
};

export default {
  uploadCv,
  getCvStatus,
  searchCandidates,
};
