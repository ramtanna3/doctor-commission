import { useEffect, useState } from 'react';
import LoadingSpinner from './components/LoadingSpinner';
import GlobalSnackbar from './components/GlobalSnackbar';

export default function ActiveCommissions() {
  const [commissions, setCommissions] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchActiveCommissions();
  }, []);

  const fetchActiveCommissions = async () => {
    setLoading(true);
    setError(null);
    try {
  const res = await fetch('/api/commissions/active', { headers: { 'x-user-id': '1' } });
      if (!res.ok) throw new Error('Failed to fetch active commissions');
      setCommissions(await res.json());
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 900, margin: '0 auto' }}>
      <h2>Active Promotionals</h2>
  {loading && <LoadingSpinner />}
  <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
      <table border={1} cellPadding={6} style={{ marginTop: 20, width: '100%' }}>
        <thead>
          <tr>
            <th>Doctor</th>
            <th>Medical</th>
            <th>Product</th>
            <th>Promotional %</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {commissions.map((c: any) => (
            <tr key={c.commissionId || c.id}>
              <td>{c.doctor?.name || ''}</td>
              <td>{c.medical?.name || ''}</td>
              <td>{c.product?.name || ''}</td>
              <td>{c.commissionPercentage}</td>
              <td>{c.isActive ? 'Active' : 'Inactive'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
