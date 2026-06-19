import axiosInstance from './axios.js';

export async function getDoctors() {
  const { data } = await axiosInstance.get('/doctors');
  return data;
}

export async function getCurrentDoctor() {
  const { data } = await axiosInstance.get('/doctors/me');
  return data;
}

export async function createDoctorProfile(payload) {
  const { data } = await axiosInstance.post('/doctors', payload);
  return data;
}

export async function updateDoctorProfile(id, payload) {
  const { data } = await axiosInstance.put(`/doctors/${id}`, payload);
  return data;
}

export async function deleteDoctor(id) {
  const { data } = await axiosInstance.delete(`/doctors/${id}`);
  return data;
}

export async function getDoctorSpecializations() {
  const { data } = await axiosInstance.get('/doctors/specializations');
  return data;
}
