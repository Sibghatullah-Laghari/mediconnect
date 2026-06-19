import { useMemo, useState } from 'react';
import EmptyState from '../../components/common/EmptyState.jsx';
import LoadingSpinner from '../../components/common/LoadingSpinner.jsx';
import PageHeader from '../../components/common/PageHeader.jsx';
import Button from '../../components/ui/Button.jsx';
import { Card, CardContent } from '../../components/ui/Card.jsx';
import Input from '../../components/ui/Input.jsx';
import { useDeleteDoctorMutation, useDoctors } from '../../hooks/useDoctors.js';
import { useToast } from '../../hooks/useToast.jsx';
import { getErrorMessage } from '../../utils/errorUtils.js';

export default function DoctorsManagement() {
  const [search, setSearch] = useState('');
  const doctors = useDoctors();
  const deleteMutation = useDeleteDoctorMutation();
  const toast = useToast();

  const filtered = useMemo(
    () =>
      (doctors.data || []).filter((doctor) =>
        `${doctor.name} ${doctor.email} ${doctor.specialization}`.toLowerCase().includes(search.toLowerCase())
      ),
    [doctors.data, search]
  );

  if (doctors.isLoading) {
    return <LoadingSpinner label="Loading doctors" />;
  }

  return (
    <div className="space-y-6">
      <PageHeader eyebrow="Admin doctors" title="Manage doctors" description="Search and administer doctor records." />
      <Card>
        <CardContent className="p-5">
          <Input placeholder="Search doctors" value={search} onChange={(event) => setSearch(event.target.value)} />
        </CardContent>
      </Card>
      {!filtered.length ? (
        <EmptyState title="No doctors found" description="Try a different search query." />
      ) : (
        <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500">
              <tr>
                <th className="px-4 py-3 font-medium">Name</th>
                <th className="px-4 py-3 font-medium">Specialization</th>
                <th className="px-4 py-3 font-medium">Email</th>
                <th className="px-4 py-3 font-medium">Action</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((doctor, index) => (
                <tr key={doctor.id} className={index % 2 === 0 ? 'bg-white' : 'bg-slate-50/60'}>
                  <td className="px-4 py-3">{doctor.name}</td>
                  <td className="px-4 py-3">{doctor.specialization}</td>
                  <td className="px-4 py-3">{doctor.email}</td>
                  <td className="px-4 py-3">
                    <Button
                      size="sm"
                      variant="destructive"
                      loading={deleteMutation.isPending}
                      onClick={async () => {
                        try {
                          await deleteMutation.mutateAsync(doctor.id);
                          toast.success('Doctor deleted successfully.');
                        } catch (error) {
                          toast.error(getErrorMessage(error, 'Unable to delete doctor.'));
                        }
                      }}
                    >
                      Delete
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
