import { ArrowRight, CalendarDays, ShieldCheck, Stethoscope } from 'lucide-react';
import { Link } from 'react-router-dom';
import Button from '../../components/ui/Button.jsx';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card.jsx';
import { ROUTES } from '../../constants/routes.js';

export default function LandingPage() {
  return (
    <div className="grid gap-10 lg:grid-cols-[1.2fr_0.8fr] lg:items-center">
      <div>
        <p className="text-xs uppercase tracking-wide text-slate-500">Healthcare appointment platform</p>
        <h1 className="mt-3 text-4xl font-semibold tracking-tight text-slate-900">
          A clean, connected workflow for patients, doctors, and clinic admins.
        </h1>
        <p className="mt-4 max-w-2xl text-sm leading-7 text-slate-600">
          MediConnect helps clinical teams coordinate appointments, manage profiles, and keep each role focused on the data they actually need.
        </p>
        <div className="mt-8 flex flex-wrap gap-4">
          <Link to={ROUTES.login}>
            <Button size="lg">
              Sign in
              <ArrowRight className="h-4 w-4" />
            </Button>
          </Link>
          <Link to={ROUTES.register}>
            <Button size="lg" variant="outline">
              Create account
            </Button>
          </Link>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>What’s included</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4">
          {[
            ['Role-aware dashboards', 'Different experiences for patients, doctors, and admins.', CalendarDays],
            ['Live API integration', 'All views are driven by the backend, not placeholder state.', ShieldCheck],
            ['Clinical clarity', 'Readable tables, concise cards, and medical-blue visual hierarchy.', Stethoscope],
          ].map(([title, description, Icon]) => (
            <div key={title} className="flex gap-3 rounded-lg border border-slate-200 p-4">
              <div className="flex h-10 w-10 items-center justify-center rounded-md bg-blue-50 text-primary">
                <Icon className="h-5 w-5" />
              </div>
              <div>
                <p className="font-medium text-slate-900">{title}</p>
                <p className="mt-1 text-sm text-slate-600">{description}</p>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
