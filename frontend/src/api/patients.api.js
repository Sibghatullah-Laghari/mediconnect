import axiosInstance from './axios.js';

export async function getPatients() {
  const { data } = await axiosInstance.get('/patients');
  return data;
}

export async function getCurrentPatient() {
  const { data } = await axiosInstance.get('/patients/me');
  return data;
}

export async function createPatientProfile(payload) {
  const { data } = await axiosInstance.post('/patients', payload);
  return data;
}

export async function updatePatientProfile(id, payload) {
  const { data } = await axiosInstance.put(`/patients/${id}`, payload);
  return data;
}

export async function deletePatient(id) {
  const { data } = await axiosInstance.delete(`/patients/${id}`);
  return data;
}
