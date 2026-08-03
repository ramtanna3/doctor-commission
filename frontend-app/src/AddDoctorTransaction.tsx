import React, { useEffect, useState } from 'react';
import { apiFetch } from './api';
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
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Autocomplete from '@mui/material/Autocomplete';
import Checkbox from '@mui/material/Checkbox';
import FormControlLabel from '@mui/material/FormControlLabel';

const REFERENCE_TYPE_OPTIONS = [
  { value: 'ADVANCE_CREDIT', label: 'Advance Credit' },
  { value: 'SALE_COMMISSION', label: 'Sale Promotional' },
  { value: 'PAYOUT', label: 'Payout' },
  { value: 'ADJUSTMENT', label: 'Adjustment' },
];

export default function AddDoctorTransaction({ onSuccess }: { onSuccess?: () => void }) {
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
  const [walletBalance, setWalletBalance] = useState<number | null>(null);
  const [useTotalDue, setUseTotalDue] = useState(false);
  const [transactionSummary, setTransactionSummary] = useState<any>(null);

  useEffect(() => {
    fetchDoctors();
  }, []);

  const fetchDoctors = async () => {
    try {
        const res = await apiFetch('/api/doctors');
      if (!res.ok) throw new Error('Failed to fetch doctors');
      setDoctors(await res.json());
    } catch (e: any) {
      setError(e.message);
    }
  };

  const fetchWalletBalance = async (selectedDoctorId: string) => {
    if (!selectedDoctorId) {
      setWalletBalance(null);
      setUseTotalDue(false);
      return;
    }

    try {
      const res = await apiFetch(`/api/doctor-wallet/balance/${selectedDoctorId}`);
      if (!res.ok) throw new Error('Failed to fetch wallet balance');
      const data = await res.json();
      setWalletBalance(data.balance ?? null);
      setUseTotalDue(false);
    } catch (e: any) {
      setWalletBalance(null);
      setUseTotalDue(false);
      setError(e.message);
    }
  };

  const handleReferenceTypeChange = (type: string) => {
    // clear any Total Due selection and reset credit when changing type
    setUseTotalDue(false);
    setCreditAmount('');
    setReferenceType(type);
    if (type === 'ADVANCE_CREDIT' || type === 'PAYOUT') {
      setDebitAmount('0');
      if (!useTotalDue) {
        setCreditAmount('');
      }
    } else if (type === 'SALE_COMMISSION') {
      setCreditAmount('0');
      setDebitAmount('');
    } else {
      if (!useTotalDue) {
        setCreditAmount('');
      }
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
    ) return setError('Debit amount must be positive for Sale Promotional.');
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
      const res = await apiFetch('/api/doctor-wallet/add-transaction', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error('Failed to add transaction');

      const respData = await res.json();

      // Determine new balance from response if available
      let newBalance: number | null = null;
      if (respData) {
        newBalance = respData.balance ?? respData.updatedBalance ?? respData.walletBalance ?? null;
      }

      // If backend didn't return a balance, fetch it fresh
      if (newBalance === null) {
        try {
          const balRes = await apiFetch(`/api/doctor-wallet/balance/${doctorId}`);
          if (balRes.ok) {
            const balData = await balRes.json();
            newBalance = balData.balance ?? null;
            setWalletBalance(newBalance);
          }
        } catch (ignore) {
          // keep newBalance as null
        }
      } else {
        setWalletBalance(newBalance);
      }

      // Find doctor name if available
      const doctorObj = doctors.find(d => String(d.doctorId || d.id) === String(doctorId));
      const doctorName = doctorObj?.name || '';

      // Recent transaction info (try to use explicit field, else use response body)
      const recentTx = respData?.transaction ?? respData;

      setTransactionSummary({ doctorId, doctorName, balance: newBalance, transaction: recentTx });

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
                onChange={(_e: any, newValue: any) => {
                  const nextDoctorId = newValue ? (newValue.doctorId || newValue.id) : '';
                  setDoctorId(nextDoctorId);
                  setUseTotalDue(false);
                  setCreditAmount('');
                  if (nextDoctorId) {
                    void fetchWalletBalance(String(nextDoctorId));
                  } else {
                    setWalletBalance(null);
                  }
                }}
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
            {walletBalance !== null && (
              <Typography sx={{ fontWeight: 600, color: walletBalance < 0 ? 'error.main' : 'success.main' }}>
                Wallet Balance: {walletBalance.toFixed(2)}
              </Typography>
            )}
            {(referenceType === 'PAYOUT' || referenceType === 'ADJUSTMENT') && walletBalance !== null && walletBalance < 0 && (
              <FormControlLabel
                control={
                  <Checkbox
                    checked={useTotalDue}
                    onChange={(e) => {
                      const checked = e.target.checked;
                      setUseTotalDue(checked);
                      if (checked) {
                        setCreditAmount(Math.abs(walletBalance).toFixed(2));
                      } else {
                        setCreditAmount('');
                      }
                    }}
                  />
                }
                label={`Pay Total Due (${Math.abs(walletBalance).toFixed(2)})`}
              />
            )}
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
              <Button type="button" variant="outlined" color="secondary" disabled={loading} sx={{ minWidth: 100, height: 40, whiteSpace: 'nowrap', fontWeight: 600, fontSize: 16, px: 2 }} onClick={() => { setDoctorId(''); setReferenceType('ADVANCE_CREDIT'); setCreditAmount(''); setDebitAmount(''); setRemarks(''); setWalletBalance(null); setUseTotalDue(false); }}>
                Cancel
              </Button>
            </Box>
            {loading && <LoadingSpinner />}
            <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
            <SuccessDialog
              open={showSuccessDialog}
              message={success || ''}
              onClose={() => { setShowSuccessDialog(false); setTransactionSummary(null); if (onSuccess) onSuccess(); }}
              details={transactionSummary ? (
                <Box sx={{ textAlign: 'left', px: 1 }}>
                  <Typography sx={{ fontWeight: 600 }}>Doctor: {transactionSummary.doctorName || `#${transactionSummary.doctorId}`}</Typography>
                  <Typography>Doctor ID: {transactionSummary.doctorId}</Typography>
                  <Typography>Wallet Balance: {transactionSummary.balance !== null && transactionSummary.balance !== undefined ? transactionSummary.balance.toFixed(2) : 'N/A'}</Typography>
                  {transactionSummary.transaction && (
                    <Box sx={{ mt: 1 }}>
                      <Typography sx={{ fontWeight: 600, mb: 1 }}>Recent Transaction</Typography>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>Type</TableCell>
                            <TableCell align="right">Credit</TableCell>
                            <TableCell align="right">Debit</TableCell>
                            <TableCell>Remarks</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          <TableRow>
                            <TableCell>{transactionSummary.transaction.referenceType || transactionSummary.transaction.type || 'N/A'}</TableCell>
                            <TableCell align="right">{transactionSummary.transaction.creditAmount ?? transactionSummary.transaction.credit ?? 'N/A'}</TableCell>
                            <TableCell align="right">{transactionSummary.transaction.debitAmount ?? transactionSummary.transaction.debit ?? 'N/A'}</TableCell>
                            <TableCell>{transactionSummary.transaction.remarks ?? transactionSummary.transaction.note ?? 'N/A'}</TableCell>
                          </TableRow>
                        </TableBody>
                      </Table>
                    </Box>
                  )}
                </Box>
              ) : undefined}
            />
          </Stack>
        </form>
      </CardContent>
    </Card>
  );
}
