import axiosInstance from './axios.js';

export async function getAppointments() {
  const { data } = await axiosInstance.get('/appointments');
  return data;
}

export async function getAppointmentsByPatient(patientId) {
  const { data } = await axiosInstance.get(`/appointments/patient/${patientId}`);
  return data;
}

export async function getAppointmentsByDoctor(doctorId) {
  const { data } = await axiosInstance.get(`/appointments/doctor/${doctorId}`);
  return data;
}

export async function createAppointment(payload) {
  const { data } = await axiosInstance.post('/appointments', payload);
  return data;
}

export async function updateAppointment(id, payload) {
  const { data } = await axiosInstance.put(`/appointments/${id}`, payload);
  return data;
}

export async function confirmAppointment(id) {
  const { data } = await axiosInstance.patch(`/appointments/${id}/confirm`);
  return data;
}

export async function completeAppointment(id) {
  const { data } = await axiosInstance.patch(`/appointments/${id}/complete`);
  return data;
}

export async function cancelAppointment(id) {
  const { data } = await axiosInstance.patch(`/appointments/${id}/cancel`);
  return data;
}

export async function updateAppointmentStatus(id, status) {
  const { data } = await axiosInstance.patch(`/appointments/${id}/status`, { status });
  return data;
}

export async function deleteAppointment(id) {
  const { data } = await axiosInstance.delete(`/appointments/${id}`);
  return data;
}
