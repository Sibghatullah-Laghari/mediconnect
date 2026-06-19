import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createPatientProfile,
  deletePatient,
  getCurrentPatient,
  getPatients,
  updatePatientProfile,
} from '../api/patients.api.js';
import { queryKeys } from '../constants/queryKeys.js';

export function usePatients(enabled = true) {
  return useQuery({
    queryKey: queryKeys.patients,
    queryFn: getPatients,
    enabled,
  });
}

export function usePatientProfile() {
  return useQuery({
    queryKey: queryKeys.patientProfile,
    queryFn: getCurrentPatient,
    retry: false,
  });
}

export function usePatientProfileMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }) =>
      id ? updatePatientProfile(id, payload) : createPatientProfile(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.patientProfile });
      queryClient.invalidateQueries({ queryKey: queryKeys.patients });
    },
  });
}

export function useDeletePatientMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deletePatient,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.patients });
    },
  });
}
