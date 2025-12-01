// Debug utility to inspect the structure of the doctor ledger API response
import { useEffect, useState } from 'react';
import LoadingSpinner from './components/LoadingSpinner';
import GlobalSnackbar from './components/GlobalSnackbar';

export default function DebugDoctorLedger() {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [doctorId, setDoctorId] = useState('');

  const fetchLedger = async () => {
    setLoading(true);
    setError(null);
    try {
  const res = await fetch(`/api/doctor-wallet/ledger/${doctorId}`, { headers: { 'x-user-id': '1' } });
      if (!res.ok) throw new Error('Failed to fetch doctor ledger');
      setData(await res.json());
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 900, margin: '0 auto' }}>
      <h2>Debug Doctor Ledger API</h2>
      <input
        type="text"
        placeholder="Enter Doctor ID"
        value={doctorId}
        onChange={e => setDoctorId(e.target.value)}
        style={{ marginRight: 8 }}
      />
      <button onClick={fetchLedger} disabled={!doctorId || loading}>
        Fetch
      </button>
  {loading && <LoadingSpinner />}
  <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
      <pre style={{ background: '#f5f5f5', padding: 16, fontSize: 14, marginTop: 20 }}>
        {JSON.stringify(data, null, 2)}
      </pre>
    </div>
  );
}
