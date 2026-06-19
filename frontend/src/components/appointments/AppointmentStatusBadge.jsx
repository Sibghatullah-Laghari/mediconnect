import { statusConfig, statusLabels } from '../../constants/statusConfig.js';

export default function AppointmentStatusBadge({ status }) {
  const style = statusConfig[status] || 'border-slate-200 bg-slate-100 text-slate-700';
  return (
    <span className={`inline-flex rounded-md border px-2.5 py-1 text-xs font-medium ${style}`}>
      {statusLabels[status] || status}
    </span>
  );
}
