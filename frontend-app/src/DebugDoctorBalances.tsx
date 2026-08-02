// Utility to inspect the structure of the balances API response for debugging
import { useEffect, useState } from 'react';
import LoadingSpinner from './components/LoadingSpinner';
import GlobalSnackbar from './components/GlobalSnackbar';
import { apiFetch } from './api';

export default function DebugDoctorBalances() {
  const [balances, setBalances] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchBalances();
  }, []);

  const fetchBalances = async () => {
    setLoading(true);
    setError(null);
    try {
  const res = await apiFetch('/api/doctor-wallet/balance/company');
      if (!res.ok) throw new Error('Failed to fetch doctor balances');
      setBalances(await res.json());
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 900, margin: '0 auto' }}>
      <h2>Debug Doctor Balances API</h2>
  {loading && <LoadingSpinner />}
  <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
      <pre style={{ background: '#f5f5f5', padding: 16, fontSize: 14, marginTop: 20 }}>
        {JSON.stringify(balances, null, 2)}
      </pre>
    </div>
  );
}
