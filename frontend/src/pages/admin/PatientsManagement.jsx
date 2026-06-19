import { useMemo, useState } from 'react';
import EmptyState from '../../components/common/EmptyState.jsx';
import LoadingSpinner from '../../components/common/LoadingSpinner.jsx';
import PageHeader from '../../components/common/PageHeader.jsx';
import Button from '../../components/ui/Button.jsx';
import { Card, CardContent } from '../../components/ui/Card.jsx';
import Input from '../../components/ui/Input.jsx';
import { useDeletePatientMutation, usePatients } from '../../hooks/usePatients.js';
import { useToast } from '../../hooks/useToast.jsx';
import { getErrorMessage } from '../../utils/errorUtils.js';

export default function PatientsManagement() {
  const [search, setSearch] = useState('');
  const patients = usePatients();
  const deleteMutation = useDeletePatientMutation();
  const toast = useToast();

  const filtered = useMemo(
    () =>
      (patients.data || []).filter((patient) =>
        `${patient.name} ${patient.email}`.toLowerCase().includes(search.toLowerCase())
      ),
    [patients.data, search]
  );

  if (patients.isLoading) {
    return <LoadingSpinner label="Loading patients" />;
  }

  return (
    <div className="space-y-6">
      <PageHeader eyebrow="Admin patients" title="Manage patients" description="Search and remove patient records when needed." />
      <Card>
        <CardContent className="p-5">
          <Input placeholder="Search patients by name or email" value={search} onChange={(event) => setSearch(event.target.value)} />
        </CardContent>
      </Card>
      {!filtered.length ? (
        <EmptyState title="No patients found" description="Try a different search query." />
      ) : (
        <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-500">
              <tr>
                <th className="px-4 py-3 font-medium">Name</th>
                <th className="px-4 py-3 font-medium">Email</th>
                <th className="px-4 py-3 font-medium">Phone</th>
                <th className="px-4 py-3 font-medium">Action</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((patient, index) => (
                <tr key={patient.id} className={index % 2 === 0 ? 'bg-white' : 'bg-slate-50/60'}>
                  <td className="px-4 py-3">{patient.name}</td>
                  <td className="px-4 py-3">{patient.email}</td>
                  <td className="px-4 py-3">{patient.phone}</td>
                  <td className="px-4 py-3">
                    <Button
                      size="sm"
                      variant="destructive"
                      loading={deleteMutation.isPending}
                      onClick={async () => {
                        try {
                          await deleteMutation.mutateAsync(patient.id);
                          toast.success('Patient deleted successfully.');
                        } catch (error) {
                          toast.error(getErrorMessage(error, 'Unable to delete patient.'));
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
