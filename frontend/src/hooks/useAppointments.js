import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  cancelAppointment,
  completeAppointment,
  confirmAppointment,
  createAppointment,
  deleteAppointment,
  getAppointments,
  getAppointmentsByDoctor,
  getAppointmentsByPatient,
  updateAppointment,
  updateAppointmentStatus,
} from '../api/appointments.api.js';
import { queryKeys } from '../constants/queryKeys.js';

export function useAppointments(enabled = true) {
  return useQuery({
    queryKey: queryKeys.appointments,
    queryFn: getAppointments,
    enabled,
  });
}

export function usePatientAppointments(patientId) {
  return useQuery({
    queryKey: [...queryKeys.appointments, 'patient', patientId],
    queryFn: () => getAppointmentsByPatient(patientId),
    enabled: Boolean(patientId),
  });
}

export function useDoctorAppointments(doctorId) {
  return useQuery({
    queryKey: [...queryKeys.appointments, 'doctor', doctorId],
    queryFn: () => getAppointmentsByDoctor(doctorId),
    enabled: Boolean(doctorId),
  });
}

export function useAppointmentMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }) => (id ? updateAppointment(id, payload) : createAppointment(payload)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.appointments });
    },
  });
}

export function useAppointmentStatusMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, action, status }) => {
      if (action === 'confirm') return confirmAppointment(id);
      if (action === 'complete') return completeAppointment(id);
      if (action === 'cancel') return cancelAppointment(id);
      if (action === 'delete') return deleteAppointment(id);
      return updateAppointmentStatus(id, status);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.appointments });
    },
  });
}
