import { format, isAfter, isBefore, isToday, parseISO } from 'date-fns';

export function formatAppointmentDate(date, time) {
  if (!date) return 'Unknown date';
  const parsedDate = typeof date === 'string' ? parseISO(date) : date;
  return `${format(parsedDate, 'MMM d, yyyy')}${time ? ` at ${time}` : ''}`;
}

export function isUpcomingAppointment(appointment) {
  const dateTime = new Date(`${appointment.appointmentDate}T${appointment.appointmentTime}`);
  return isAfter(dateTime, new Date()) || isToday(dateTime);
}

export function isPastAppointment(appointment) {
  const dateTime = new Date(`${appointment.appointmentDate}T${appointment.appointmentTime}`);
  return isBefore(dateTime, new Date());
}
