import { CheckCircle2, CircleOff, Trash2 } from 'lucide-react';
import AppointmentStatusBadge from './AppointmentStatusBadge.jsx';
import { formatAppointmentDate } from '../../utils/dateUtils.js';

export default function AppointmentTable({ appointments, onAction, role }) {
  return (
    <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
      <table className="min-w-full text-left text-sm">
        <thead className="bg-slate-50 text-slate-500">
          <tr>
            <th className="px-4 py-3 font-medium">Patient</th>
            <th className="px-4 py-3 font-medium">Doctor</th>
            <th className="px-4 py-3 font-medium">When</th>
            <th className="px-4 py-3 font-medium">Reason</th>
            <th className="px-4 py-3 font-medium">Status</th>
            <th className="px-4 py-3 font-medium">Actions</th>
          </tr>
        </thead>
        <tbody>
          {appointments.map((appointment, index) => (
            <tr key={appointment.id} className={index % 2 === 0 ? 'bg-white' : 'bg-slate-50/60'}>
              <td className="px-4 py-3 text-slate-700">{appointment.patientName}</td>
              <td className="px-4 py-3 text-slate-700">{appointment.doctorName}</td>
              <td className="px-4 py-3 text-slate-700">
                {formatAppointmentDate(appointment.appointmentDate, appointment.appointmentTime)}
              </td>
              <td className="px-4 py-3 text-slate-700">{appointment.reason}</td>
              <td className="px-4 py-3">
                <AppointmentStatusBadge status={appointment.status} />
              </td>
              <td className="px-4 py-3">
                <div className="flex items-center gap-2">
                  {role === 'DOCTOR' && appointment.status === 'PENDING' ? (
                    <button
                      type="button"
                      className="rounded-md border border-emerald-200 p-2 text-emerald-700 hover:bg-emerald-50"
                      onClick={() => onAction?.('confirm', appointment)}
                      title="Confirm appointment"
                    >
                      <CheckCircle2 className="h-4 w-4" />
                    </button>
                  ) : null}
                  {role === 'DOCTOR' && appointment.status === 'CONFIRMED' ? (
                    <button
                      type="button"
                      className="rounded-md border border-blue-200 p-2 text-blue-700 hover:bg-blue-50"
                      onClick={() => onAction?.('complete', appointment)}
                      title="Mark completed"
                    >
                      <CheckCircle2 className="h-4 w-4" />
                    </button>
                  ) : null}
                  {(role === 'PATIENT' || role === 'ADMIN' || role === 'DOCTOR') &&
                  appointment.status !== 'COMPLETED' &&
                  appointment.status !== 'CANCELLED' ? (
                    <button
                      type="button"
                      className="rounded-md border border-amber-200 p-2 text-amber-700 hover:bg-amber-50"
                      onClick={() => onAction?.('cancel', appointment)}
                      title="Cancel appointment"
                    >
                      <CircleOff className="h-4 w-4" />
                    </button>
                  ) : null}
                  {role === 'ADMIN' ? (
                    <button
                      type="button"
                      className="rounded-md border border-red-200 p-2 text-red-700 hover:bg-red-50"
                      onClick={() => onAction?.('delete', appointment)}
                      title="Delete appointment"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  ) : null}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
