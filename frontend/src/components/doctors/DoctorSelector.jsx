export default function DoctorSelector({ doctors, value, onChange }) {
  return (
    <div className="grid gap-3">
      {doctors.map((doctor) => (
        <button
          key={doctor.id}
          type="button"
          onClick={() => onChange(doctor.id)}
          className={`rounded-lg border p-4 text-left transition ${
            value === doctor.id ? 'border-primary bg-blue-50' : 'border-slate-200 bg-white hover:border-slate-300'
          }`}
        >
          <p className="font-medium text-slate-900">{doctor.name}</p>
          <p className="text-sm text-slate-600">{doctor.specialization}</p>
          <p className="text-sm text-slate-500">{doctor.email}</p>
        </button>
      ))}
    </div>
  );
}
