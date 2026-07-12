import { useEffect, useState } from 'react';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import LoadingSpinner from './components/LoadingSpinner';
import GlobalSnackbar from './components/GlobalSnackbar';
import SuccessDialog from './components/SuccessDialog';
import Box from '@mui/material/Box';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';

export default function SyncDoctorWallet() {
  const [doctors, setDoctors] = useState<any[]>([]);
  const [selectedDoctor, setSelectedDoctor] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showSuccessDialog, setShowSuccessDialog] = useState(false);

  useEffect(() => {
    fetchDoctors();
  }, []);

  const fetchDoctors = async () => {
    try {
  const res = await fetch('/api/doctors', { headers: { 'x-user-id': '1' } });
      if (!res.ok) throw new Error('Failed to fetch doctors');
      setDoctors(await res.json());
    } catch (e: any) {
      setError(e.message);
    }
  };

  const handleSyncDoctor = async () => {
    setLoading(true);
    setError(null);
    try {
  const res = await fetch(`/api/doctor-wallet/sync/${selectedDoctor}`, { method: 'POST', headers: { 'x-user-id': '1' } });
      if (!res.ok) throw new Error('Failed to sync doctor wallet');
      setSuccess('Doctor wallet synced successfully!');
      setShowSuccessDialog(true);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSyncAll = async () => {
    setLoading(true);
    setError(null);
    try {
  const res = await fetch('/api/doctor-wallet/sync/all', { method: 'POST', headers: { 'x-user-id': '1' } });
      if (!res.ok) throw new Error('Failed to sync all doctor wallets');
      setSuccess('All doctor wallets synced successfully!');
      setShowSuccessDialog(true);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ flex: 1, width: '100%', minHeight: 'calc(100vh - 64px)', background: '#f5f5f5', p: { xs: 1, sm: 1 }, boxSizing: 'border-box', display: 'flex', flexDirection: 'column' }}>
      <Card sx={{ width: '100%', maxWidth: 600, m: '0 auto', mt: 4, boxShadow: 2, borderRadius: 3, background: '#f9fafb' }}>
        <CardContent sx={{ p: { xs: 2, sm: 3 } }}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1976d2', mb: 2, pl: 1 }}>
            Sync Doctor Wallet
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap', mb: 3 }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#555', minWidth: 60 }}>
              Filters
            </Typography>
            <Autocomplete
              getOptionKey={option => option.doctorId || option.id}
              options={doctors}
              getOptionLabel={(d: any) => d?.name || ''}
              value={doctors.find((d: any) => (d.doctorId || d.id) === selectedDoctor) || null}
              onChange={(_e: any, newValue: any) => setSelectedDoctor(newValue ? (newValue.doctorId || newValue.id) : '')}
              isOptionEqualToValue={(option: any, value: any) => (option.doctorId || option.id) === (value.doctorId || value.id)}
              filterOptions={(options, { inputValue }) =>
                options.filter((d: any) => (d?.name || '').toLowerCase().includes(inputValue.trim().toLowerCase()))
              }
              renderInput={(params: any) => (
                <TextField {...params} label="Select Doctor" size="small" required sx={{ minWidth: 220, maxWidth: 340, background: '#fff', borderRadius: 1 }} />
              )}
              sx={{ minWidth: 220, maxWidth: 340 }}
              disableClearable
            />
          </Box>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 2 }}>
            <Button
              onClick={handleSyncDoctor}
              disabled={!selectedDoctor || loading}
              variant="contained"
              color="primary"
              sx={{ fontWeight: 600, fontSize: 16, minWidth: 180, height: 40, boxShadow: 1 }}
            >
              {loading && selectedDoctor ? 'Syncing...' : 'Sync Selected Doctor'}
            </Button>
            <Button
              onClick={handleSyncAll}
              disabled={loading}
              variant="contained"
              color="secondary"
              sx={{ fontWeight: 600, fontSize: 16, minWidth: 180, height: 40, boxShadow: 1 }}
            >
              {loading && !selectedDoctor ? 'Syncing...' : 'Sync All Doctors'}
            </Button>
          </Box>
          {loading && <LoadingSpinner />}
          <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
          <SuccessDialog open={showSuccessDialog} message={success || ''} onClose={() => setShowSuccessDialog(false)} />
        </CardContent>
      </Card>
    </Box>
  );
}
