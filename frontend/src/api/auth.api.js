import axiosInstance from './axios.js';

export async function loginUser(payload) {
  const { data } = await axiosInstance.post('/auth/login', payload);
  return data;
}

export async function registerUser(payload) {
  const { data } = await axiosInstance.post('/auth/register', payload);
  return data;
}

export async function getCurrentUser() {
  const { data } = await axiosInstance.get('/auth/me');
  return data;
}
