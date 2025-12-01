import React, { useEffect, useState } from 'react';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import LoadingSpinner from './components/LoadingSpinner';
import GlobalSnackbar from './components/GlobalSnackbar';
import SuccessDialog from './components/SuccessDialog';
import Stack from '@mui/material/Stack';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import TextField from '@mui/material/TextField';
import Box from '@mui/material/Box';
import Autocomplete from '@mui/material/Autocomplete';

const REFERENCE_TYPE_OPTIONS = [
  { value: 'ADVANCE_CREDIT', label: 'Advance Credit' },
  { value: 'SALE_COMMISSION', label: 'Sale Commission' },
  { value: 'PAYOUT', label: 'Payout' },
  { value: 'ADJUSTMENT', label: 'Adjustment' },
];

export default function AddDoctorTransaction() {
  const [showSuccessDialog, setShowSuccessDialog] = useState(false);
  const [doctors, setDoctors] = useState<any[]>([]);
  const [doctorId, setDoctorId] = useState('');
  const [referenceType, setReferenceType] = useState('ADVANCE_CREDIT');
  const [creditAmount, setCreditAmount] = useState('');
  const [debitAmount, setDebitAmount] = useState('');
  const [remarks, setRemarks] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

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

  const handleReferenceTypeChange = (type: string) => {
    setReferenceType(type);
    if (type === 'ADVANCE_CREDIT' || type === 'PAYOUT') {
      setDebitAmount('0');
      setCreditAmount('');
    } else if (type === 'SALE_COMMISSION') {
      setCreditAmount('0');
      setDebitAmount('');
    } else {
      setCreditAmount('');
      setDebitAmount('');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    if (!doctorId) return setError('Please select a doctor.');
    if (!referenceType) return setError('Please select a reference type.');
    if (
      referenceType === 'ADVANCE_CREDIT' && (!creditAmount || Number(creditAmount) <= 0)
    ) return setError('Credit amount must be positive for Advance Credit.');
    if (
      referenceType === 'SALE_COMMISSION' && (!debitAmount || Number(debitAmount) <= 0)
    ) return setError('Debit amount must be positive for Sale Commission.');
    if (
      referenceType === 'PAYOUT' && (!creditAmount || Number(creditAmount) <= 0)
    ) return setError('Credit amount must be positive for Payout.');
    if (
      referenceType === 'ADJUSTMENT' && (!creditAmount && !debitAmount)
    ) return setError('Enter a value in either Credit or Debit for Adjustment.');
    if (
      referenceType === 'ADJUSTMENT' && Number(creditAmount) > 0 && Number(debitAmount) > 0
    ) return setError('Only one of Credit or Debit should be positive for Adjustment.');

    setLoading(true);
    try {
      const payload = {
        doctorId: Number(doctorId),
        referenceType,
        creditAmount: Number(creditAmount) || 0,
        debitAmount: Number(debitAmount) || 0,
        remarks,
      };
      const res = await fetch('/api/doctor-wallet/add-transaction', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
            'x-user-id': '1',
        },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error('Failed to add transaction');
  setSuccess('Transaction added successfully!');
  setShowSuccessDialog(true);
  setCreditAmount('');
  setDebitAmount('');
  setRemarks('');
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card sx={{ width: '100%', maxWidth: 1100, m: '0 auto', mt: 4, boxShadow: 2, borderRadius: 3, background: '#f9fafb' }}>
      <CardContent sx={{ p: { xs: 2, sm: 3 } }}>
        <Typography variant="h6" sx={{ fontWeight: 700, color: '#1976d2', mb: 2, pl: 1 }}>
          Add Doctor Transaction
        </Typography>
        <form onSubmit={handleSubmit} style={{ width: '100%' }}>
          <Stack spacing={3}>
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Autocomplete
                getOptionKey={option => option.doctorId || option.id}
                options={doctors}
                getOptionLabel={(d: any) => d?.name || `Doctor #${d?.doctorId || d?.id}`}
                value={doctors.find((d: any) => String(d.doctorId || d.id) === String(doctorId)) || null}
                onChange={(_e: any, newValue: any) => setDoctorId(newValue ? (newValue.doctorId || newValue.id) : '')}
                isOptionEqualToValue={(option: any, value: any) => (option.doctorId || option.id) === (value?.doctorId || value?.id)}
                filterOptions={(options, { inputValue }) =>
                  options.filter((d: any) => (d?.name || '').toLowerCase().includes(inputValue.trim().toLowerCase()))
                }
                renderInput={(params: any) => (
                  <TextField {...params} label="Doctor" size="small" required sx={{ minWidth: 220, background: '#fff', borderRadius: 1 }} />
                )}
                sx={{ flex: 1, minWidth: 220, background: '#fff', borderRadius: 1 }}
                disableClearable
              />
              <FormControl size="small" required sx={{ flex: 1, minWidth: 220, background: '#fff', borderRadius: 1 }}>
                <InputLabel id="reference-type-label">Reference Type</InputLabel>
                <Select
                  labelId="reference-type-label"
                  value={referenceType}
                  label="Reference Type"
                  onChange={e => handleReferenceTypeChange(e.target.value)}
                  required
                >
                  {REFERENCE_TYPE_OPTIONS.map(opt => (
                    <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Box>
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <TextField
                label="Credit Amount"
                type="number"
                inputProps={{ min: 0, step: 0.01 }}
                value={creditAmount}
                onChange={e => setCreditAmount(e.target.value)}
                disabled={referenceType === 'SALE_COMMISSION'}
                required={referenceType === 'ADVANCE_CREDIT' || referenceType === 'PAYOUT'}
                size="small"
                sx={{ flex: 1, minWidth: 220, background: '#fff', borderRadius: 1 }}
              />
              <TextField
                label="Debit Amount"
                type="number"
                inputProps={{ min: 0, step: 0.01 }}
                value={debitAmount}
                onChange={e => setDebitAmount(e.target.value)}
                disabled={referenceType === 'ADVANCE_CREDIT' || referenceType === 'PAYOUT'}
                required={referenceType === 'SALE_COMMISSION'}
                size="small"
                sx={{ flex: 1, minWidth: 220, background: '#fff', borderRadius: 1 }}
              />
            </Box>
            <TextField
              label="Remarks"
              type="text"
              value={remarks}
              onChange={e => setRemarks(e.target.value)}
              required
              size="small"
              sx={{ minWidth: 220, background: '#fff', borderRadius: 1 }}
            />
            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-start', mt: 0.5 }}>
              <Button type="submit" variant="contained" color="primary" disabled={loading} sx={{ minWidth: 140, height: 40, whiteSpace: 'nowrap', fontWeight: 600, fontSize: 16, px: 2, boxShadow: 1 }}>
                {loading ? 'Adding...' : 'Add Transaction'}
              </Button>
              <Button type="button" variant="outlined" color="secondary" disabled={loading} sx={{ minWidth: 100, height: 40, whiteSpace: 'nowrap', fontWeight: 600, fontSize: 16, px: 2 }} onClick={() => { setDoctorId(''); setReferenceType('ADVANCE_CREDIT'); setCreditAmount(''); setDebitAmount(''); setRemarks(''); }}>
                Cancel
              </Button>
            </Box>
            {loading && <LoadingSpinner />}
            <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
            <SuccessDialog open={showSuccessDialog} message={success || ''} onClose={() => setShowSuccessDialog(false)} />
          </Stack>
        </form>
      </CardContent>
    </Card>
  );
}
