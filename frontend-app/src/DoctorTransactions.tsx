
import { useEffect, useMemo, useState } from 'react';
import { apiFetch } from './api';
import LoadingSpinner from './components/LoadingSpinner';
import GlobalSnackbar from './components/GlobalSnackbar';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import Autocomplete from '@mui/material/Autocomplete';
import Chip from '@mui/material/Chip';
import MenuItem from '@mui/material/MenuItem';
import TableSortLabel from '@mui/material/TableSortLabel';
import TextField from '@mui/material/TextField';
import Select from '@mui/material/Select';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Pagination from '@mui/material/Pagination';
import Stack from '@mui/material/Stack';

export default function DoctorTransactions() {
  const [doctors, setDoctors] = useState<any[]>([]);
  const [selectedDoctor, setSelectedDoctor] = useState<any>(null);
  const [doctorDetails, setDoctorDetails] = useState<any>(null);
  const [transactions, setTransactions] = useState<any[]>([]);
  const [balance, setBalance] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const REFERENCE_TYPE_OPTIONS = [
    'ADVANCE_CREDIT',
    'SALE_COMMISSION',
    'PAYOUT',
    'ADJUSTMENT',
  ];
  const [selectedReferenceTypes, setSelectedReferenceTypes] = useState<string[]>(REFERENCE_TYPE_OPTIONS);
  const [sortBy, setSortBy] = useState<string>('date');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [page, setPage] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const handleSort = (column: string) => {
    if (sortBy === column) {
      setSortOrder((prev) => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(column);
      setSortOrder('asc');
    }
  };

  useEffect(() => {
    fetchDoctors();
  }, []);

  useEffect(() => {
    if (selectedDoctor && (selectedDoctor.doctorId || selectedDoctor.id)) {
      fetchTransactions(selectedDoctor.doctorId || selectedDoctor.id);
    } else {
      setDoctorDetails(null);
      setTransactions([]);
    }
    // Do not clear transactions immediately on doctor change; let fetchTransactions handle update
    // This reduces flicker by keeping previous data until new data is fetched
  }, [selectedDoctor]);

  const fetchDoctors = async () => {
    setLoading(true);
    setError(null);
    try {
  const res = await apiFetch('/api/doctors');
      if (!res.ok) throw new Error('Failed to fetch doctors');
      const data = await res.json();
      setDoctors(data);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const fetchTransactions = async (doctorId: string | number) => {
    setLoading(true);
    setError(null);
    try {
      // Find doctor details from list
      const doc = doctors.find((d: any) => String(d.doctorId || d.id) === String(doctorId));
      // Fetch transactions
  const res = await apiFetch(`/api/doctor-wallet/ledger/${doctorId}`);
      if (!res.ok) throw new Error('Failed to fetch doctor transactions');
      const data = await res.json();
      setDoctorDetails(data.doctor || doc);
      setTransactions(data.transactions || []);

      // Fetch balance from separate API
  const balanceRes = await apiFetch(`/api/doctor-wallet/balance/${doctorId}`);
      if (balanceRes.ok) {
        const balanceData = await balanceRes.json();
        setBalance(balanceData.balance ?? null);
      } else {
        setBalance(null);
      }
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const filteredTxns = useMemo(() => transactions.filter((t: any) => {
    if (!selectedReferenceTypes.includes(t.referenceType)) return false;
    const dateStr = t.transactionDate || '';
    if (dateFrom && dateStr && dateStr < dateFrom) return false;
    if (dateTo && dateStr && dateStr > dateTo) return false;
    if (!search) return true;
    const text = [
      dateStr,
      t.referenceType,
      t.creditAmount,
      t.debitAmount,
      t.remarks
    ]
      .join(' ')
      .toLowerCase();
    return text.includes(search.toLowerCase());
  }), [transactions, search, selectedReferenceTypes, dateFrom, dateTo]);

  const sortedTxns = useMemo(() => [...filteredTxns].sort((a: any, b: any) => {
    let aVal = a[sortBy];
    let bVal = b[sortBy];
    if (sortBy === 'referenceType') {
      aVal = a.referenceType || '';
      bVal = b.referenceType || '';
    } else if (sortBy === 'date') {
      aVal = a.transactionDate || '';
      bVal = b.transactionDate || '';
    }
    if (aVal == null) aVal = '';
    if (bVal == null) bVal = '';
    if (typeof aVal === 'string') aVal = aVal.toLowerCase();
    if (typeof bVal === 'string') bVal = bVal.toLowerCase();
    if (aVal < bVal) return sortOrder === 'asc' ? -1 : 1;
    if (aVal > bVal) return sortOrder === 'asc' ? 1 : -1;
    return 0;
  }), [filteredTxns, sortBy, sortOrder]);

  const totalTxns = sortedTxns.length;
  const txPageCount = Math.max(1, Math.ceil(totalTxns / rowsPerPage));
  const txPageStart = (page - 1) * rowsPerPage;
  const txPageEnd = Math.min(txPageStart + rowsPerPage, totalTxns);
  const visibleTxns = sortedTxns.slice(txPageStart, txPageEnd);

  useEffect(() => {
    if (page > txPageCount) setPage(txPageCount);
  }, [page, txPageCount]);

  useEffect(() => {
    setPage(1);
  }, [search, selectedReferenceTypes, dateFrom, dateTo, rowsPerPage, selectedDoctor]);

  return (
    <Box sx={{ flex: 1, width: '100%', minHeight: 'calc(100vh - 64px)', background: '#f5f5f5', p: { xs: 1, sm: 1 }, boxSizing: 'border-box', display: 'flex', flexDirection: 'column', position: 'relative' }}>
      <Card sx={{ width: '100%', maxWidth: 1100, m: '0 auto', mb: 3, boxShadow: 2, borderRadius: 3, background: '#f9fafb', position: 'relative' }}>
        <CardContent sx={{ p: { xs: 2, sm: 3 }, position: 'relative' }}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1976d2', mb: 2, pl: 1 }}>
            Doctor Transactions
          </Typography>
          <Autocomplete
            getOptionKey={option => option.doctorId || option.id}
            options={doctors}
            getOptionLabel={(d: any) => d?.name || `Doctor #${d?.doctorId || d?.id}`}
            value={selectedDoctor}
            onChange={(_e: any, newValue: any) => setSelectedDoctor(newValue || null)}
            isOptionEqualToValue={(option: any, value: any) => (option.doctorId || option.id) === (value?.doctorId || value?.id)}
            filterOptions={(options, { inputValue }) =>
              options.filter((d: any) => (d?.name || '').toLowerCase().includes(inputValue.trim().toLowerCase()))
            }
            renderInput={(params: any) => (
              <TextField {...params} label="Select Doctor" size="small" required sx={{ minWidth: 220, maxWidth: 340, mb: 2, background: '#fff', borderRadius: 1 }} />
            )}
            sx={{ minWidth: 220, maxWidth: 340, mb: 2 }}
            disableClearable
          />
          {doctorDetails && (
            <Box sx={{
              position: 'absolute',
              top: 24,
              right: 32,
              minWidth: 220,
              background: 'transparent',
              boxShadow: 0,
              p: 0,
              zIndex: 2,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'flex-start',
              gap: 0.5
            }}>
              <Typography variant="body2">ID : {doctorDetails.doctorId || doctorDetails.id}</Typography>
              <Typography variant="body2">Name : {doctorDetails.name}</Typography>
              <Typography variant="body2">Specialization : {doctorDetails.specialization || 'N/A'}</Typography>
              <Typography variant="body2">Phone : {doctorDetails.phoneNumber || 'N/A'}</Typography>
              <Typography variant="body2">Email : {doctorDetails.email || 'N/A'}</Typography>
            </Box>
          )}
          {typeof balance === 'number' && (
            <Typography sx={{ mb: 2, fontWeight: 'bold', fontSize: 18 }}>
              Total Balance: <span style={{ color: balance < 0 ? 'red' : 'green' }}>{balance.toFixed(2)}</span>
            </Typography>
          )}
        </CardContent>
      </Card>
      <Card sx={{ width: '100%', maxWidth: 1100, m: '0 auto', boxShadow: 1, borderRadius: 2, flex: 1, display: 'flex', flexDirection: 'column' }}>
        <CardContent sx={{ p: 0 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', px: 3, py: 2, borderBottom: '1px solid #eee', background: '#f7f7f7', borderTopLeftRadius: 8, borderTopRightRadius: 8, gap: 2, flexWrap: 'wrap' }}>
            <Typography variant="h6" sx={{ flex: 1, fontWeight: 700, color: '#222' }}>
              Transactions
            </Typography>
            <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#555', pr: 2 }}>
              Filters
            </Typography>
            <TextField
              type="text"
              placeholder="Search transactions..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              size="small"
              sx={{ width: { xs: '100%', sm: 220 }, background: '#fff', borderRadius: 1 }}
              InputProps={{ sx: { fontSize: 16 } }}
            />
            <TextField
              type="date"
              label="From"
              value={dateFrom}
              onChange={e => setDateFrom(e.target.value)}
              size="small"
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              type="date"
              label="To"
              value={dateTo}
              onChange={e => setDateTo(e.target.value)}
              size="small"
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              select
              label="Reference Type"
              value={selectedReferenceTypes}
              onChange={e => {
                const value = e.target.value;
                setSelectedReferenceTypes(typeof value === 'string' ? value.split(',') : value);
              }}
              size="small"
              sx={{ minWidth: 180, maxWidth: 320, background: '#fff', borderRadius: 1 }}
              SelectProps={{
                multiple: true,
                renderValue: (selected: unknown) => {
                  const selectedArr = selected as string[];
                  const typeAbbr: Record<string, string> = {
                    ADVANCE_CREDIT: 'AC',
                    PAYOUT: 'PO',
                    ADJUSTMENT: 'AD',
                    SALE_COMMISSION: 'SC',
                  };
                  return (
                    <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                      {selectedArr.map(val => (
                        <Chip key={val} label={typeAbbr[val] || val} size="small" sx={{ fontWeight: 500, background: '#e0e0e0' }} />
                      ))}
                    </Box>
                  );
                },
                displayEmpty: true,
              }}
            >
              {REFERENCE_TYPE_OPTIONS.map(option => (
                <MenuItem key={option} value={option}>
                  <input
                    type="checkbox"
                    checked={selectedReferenceTypes.indexOf(option) > -1}
                    readOnly
                    style={{ marginRight: 8 }}
                  />
                  {option.replace('_', ' ').replace('ADVANCE_CREDIT', 'Advance Credit').replace('PAYOUT', 'Payout').replace('ADJUSTMENT', 'Adjustment').replace('SALE_COMMISSION', 'Sale Promotional')}
                </MenuItem>
              ))}
            </TextField>
            {/* Add more filter controls here if needed */}
          </Box>
          {loading && <LoadingSpinner />}
          <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
          <GlobalSnackbar open={!!success} message={success || ''} severity="success" onClose={() => setSuccess(null)} />
          <Box sx={{ position: 'relative' }}>
            {loading && (
              <Box sx={{
                position: 'absolute',
                top: 0,
                left: 0,
                width: '100%',
                height: '100%',
                bgcolor: 'rgba(255,255,255,0.7)',
                zIndex: 2,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}>
                <LoadingSpinner size={40} />
              </Box>
            )}
            <TableContainer component={Paper} sx={{ mt: 0, width: '100%', boxShadow: 0, borderRadius: 0 }}>
              <Table size="small" sx={{ minWidth: 900 }}>
                <TableHead sx={{ position: 'sticky', top: 0, background: '#f7f7f7', zIndex: 1 }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 700, color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                      <TableSortLabel
                        active={sortBy === 'date'}
                        direction={sortBy === 'date' ? sortOrder as 'asc' | 'desc' : 'asc'}
                        onClick={() => handleSort('date')}
                      >
                        Date
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, color: '#222', background: '#f7f7f7', fontSize: 15, minWidth: 180, maxWidth: 260 }}>
                      <TableSortLabel
                        active={sortBy === 'referenceType'}
                        direction={sortBy === 'referenceType' ? sortOrder as 'asc' | 'desc' : 'asc'}
                        onClick={() => handleSort('referenceType')}
                      >
                        Reference Type
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                      Product Name
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                      <TableSortLabel
                        active={sortBy === 'creditAmount'}
                        direction={sortBy === 'creditAmount' ? sortOrder as 'asc' | 'desc' : 'asc'}
                        onClick={() => handleSort('creditAmount')}
                      >
                        Credit Amount
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                      <TableSortLabel
                        active={sortBy === 'debitAmount'}
                        direction={sortBy === 'debitAmount' ? sortOrder as 'asc' | 'desc' : 'asc'}
                        onClick={() => handleSort('debitAmount')}
                      >
                        Debit Amount
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                      <TableSortLabel
                        active={sortBy === 'remarks'}
                        direction={sortBy === 'remarks' ? sortOrder as 'asc' | 'desc' : 'asc'}
                        onClick={() => handleSort('remarks')}
                      >
                        Remarks
                      </TableSortLabel>
                    </TableCell>
                  </TableRow>
                </TableHead>
              <TableBody>
                {visibleTxns.map((t: any, idx: number) => (
                    <TableRow key={t.doctorWalletLedgerId || t.id || idx} sx={{ background: idx % 2 === 0 ? '#fff' : '#f9fafb' }}>
                      <TableCell>{t.transactionDate || ''}</TableCell>
                      <TableCell>{t.referenceType || ''}</TableCell>
                      <TableCell>{t.productName || ''}</TableCell>
                      <TableCell>{t.creditAmount}</TableCell>
                      <TableCell>{t.debitAmount}</TableCell>
                      <TableCell>{t.remarks || ''}</TableCell>
                    </TableRow>
                  ))}
              </TableBody>
            </Table>
          </TableContainer>
          <Box sx={{ mt: 2, display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: 'center', justifyContent: 'space-between', gap: 2, px: 1 }}>
            <Typography variant="body2" sx={{ color: '#555' }}>
              Showing {totalTxns === 0 ? 0 : txPageStart + 1} - {txPageEnd} of {totalTxns} transactions
            </Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems="center" sx={{ width: { xs: '100%', sm: 'auto' } }}>
              <FormControl size="small" sx={{ minWidth: 130, background: '#fff', borderRadius: 1 }}>
                <InputLabel id="rows-per-page-label-txn">Page size</InputLabel>
                <Select
                  labelId="rows-per-page-label-txn"
                  value={rowsPerPage}
                  label="Page size"
                  onChange={e => setRowsPerPage(Number(e.target.value))}
                >
                  {[10, 20, 30].map(size => (
                    <MenuItem key={size} value={size}>{size}</MenuItem>
                  ))}
                </Select>
              </FormControl>
              <Pagination
                count={txPageCount}
                page={page}
                onChange={(_, value) => setPage(value)}
                color="primary"
                showFirstButton
                showLastButton
                shape="rounded"
              />
            </Stack>
          </Box>
          {selectedDoctor && transactions.length === 0 && !loading && <Typography sx={{ p: 2 }}>No transactions found for this doctor.</Typography>}
        </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
