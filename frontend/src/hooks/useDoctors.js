import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createDoctorProfile,
  deleteDoctor,
  getCurrentDoctor,
  getDoctors,
  getDoctorSpecializations,
  updateDoctorProfile,
} from '../api/doctors.api.js';
import { queryKeys } from '../constants/queryKeys.js';

export function useDoctors() {
  return useQuery({
    queryKey: queryKeys.doctors,
    queryFn: getDoctors,
  });
}

export function useDoctorProfile() {
  return useQuery({
    queryKey: queryKeys.doctorProfile,
    queryFn: getCurrentDoctor,
    retry: false,
  });
}

export function useDoctorSpecializations() {
  return useQuery({
    queryKey: queryKeys.specializations,
    queryFn: getDoctorSpecializations,
  });
}

export function useDoctorProfileMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }) =>
      id ? updateDoctorProfile(id, payload) : createDoctorProfile(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.doctorProfile });
      queryClient.invalidateQueries({ queryKey: queryKeys.doctors });
      queryClient.invalidateQueries({ queryKey: queryKeys.specializations });
    },
  });
}

export function useDeleteDoctorMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteDoctor,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.doctors });
      queryClient.invalidateQueries({ queryKey: queryKeys.specializations });
    },
  });
}
