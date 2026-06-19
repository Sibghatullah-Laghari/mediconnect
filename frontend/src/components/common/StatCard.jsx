import { Card, CardContent } from '../ui/Card.jsx';

export default function StatCard({ label, value, icon: Icon, tone = 'bg-blue-50 text-blue-700' }) {
  return (
    <Card>
      <CardContent className="flex items-center justify-between p-5">
        <div>
          <p className="text-xs uppercase tracking-wide text-slate-500">{label}</p>
          <p className="mt-2 text-2xl font-semibold text-slate-900">{value}</p>
        </div>
        {Icon ? (
          <div className={`flex h-11 w-11 items-center justify-center rounded-md ${tone}`}>
            <Icon className="h-5 w-5" />
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}
