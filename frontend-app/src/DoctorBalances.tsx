import { useEffect, useMemo, useState } from 'react';
import { apiFetch } from './api';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Checkbox from '@mui/material/Checkbox';
import Button from '@mui/material/Button';
import LoadingSpinner from './components/LoadingSpinner';
import GlobalSnackbar from './components/GlobalSnackbar';
import Box from '@mui/material/Box';
import TableSortLabel from '@mui/material/TableSortLabel';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Pagination from '@mui/material/Pagination';
import Stack from '@mui/material/Stack';

export default function DoctorBalances() {
  const [balances, setBalances] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('name');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [selected, setSelected] = useState<string[]>([]); // doctorId or id
  const [page, setPage] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  // Helper to get doctor id
  const getDoctorId = (b: any) => b.doctor?.doctorId || b.doctor?.id;

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

  const filtered = useMemo(() => balances.filter((b: any) => {
    const text = [
      b.doctor?.doctorId || b.doctor?.id,
      b.doctor?.name || '',
      b.doctor?.specialization || '',
      b.doctor?.phoneNumber || '',
      b.doctor?.email || '',
      b.balance
    ].join(' ').toLowerCase();
    return text.includes(search.toLowerCase());
  }), [balances, search]);

  const sorted = useMemo(() => [...filtered].sort((a: any, b: any) => {
    let aVal, bVal;
    if (sortBy === 'doctorId') {
      aVal = a.doctor?.doctorId || a.doctor?.id || '';
      bVal = b.doctor?.doctorId || b.doctor?.id || '';
    } else if (sortBy === 'balance') {
      aVal = a.balance;
      bVal = b.balance;
    } else {
      aVal = a.doctor?.[sortBy] || '';
      bVal = b.doctor?.[sortBy] || '';
    }
    if (aVal == null) aVal = '';
    if (bVal == null) bVal = '';
    if (typeof aVal === 'string') aVal = aVal.toLowerCase();
    if (typeof bVal === 'string') bVal = bVal.toLowerCase();
    if (aVal < bVal) return sortOrder === 'asc' ? -1 : 1;
    if (aVal > bVal) return sortOrder === 'asc' ? 1 : -1;
    return 0;
  }), [filtered, sortBy, sortOrder]);

  // Select all logic (only for negative balances)
  // Treat -0 as 0 for selection logic
  const isTrulyNegative = (balance: number) => Number(balance) < 0 && Math.abs(Number(balance)) > 1e-8;
  const negativeIds = sorted.filter(b => isTrulyNegative(b.balance)).map(getDoctorId);
  const isAllSelected = negativeIds.length > 0 && selected.length === negativeIds.length && negativeIds.every(id => selected.includes(id));
  const handleSelectAll = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.checked) {
      setSelected(negativeIds);
    } else {
      setSelected([]);
    }
  };

  // Select single row
  const handleSelect = (id: string) => {
    setSelected(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]);
  };

  // Helper to handle sort toggling
  const handleSort = (column: string) => {
    if (sortBy === column) {
      setSortOrder(prev => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(column);
      setSortOrder('asc');
    }
  };

  const totalBalances = sorted.length;
  const pageCount = Math.max(1, Math.ceil(totalBalances / rowsPerPage));
  const pageStart = (page - 1) * rowsPerPage;
  const pageEnd = Math.min(pageStart + rowsPerPage, totalBalances);
  const visibleBalances = sorted.slice(pageStart, pageEnd);

  useEffect(() => {
    if (page > pageCount) setPage(pageCount);
  }, [page, pageCount]);

  useEffect(() => {
    setPage(1);
  }, [search, rowsPerPage]);

  return (
    <Box sx={{ flex: 1, width: '100%', minHeight: 'calc(100vh - 64px)', background: '#f5f5f5', p: { xs: 1, sm: 1 }, boxSizing: 'border-box', display: 'flex', flexDirection: 'column' }}>
      <Card sx={{ width: '100%', maxWidth: 1100, m: '0 auto', mb: 3, boxShadow: 2, borderRadius: 3, background: '#f9fafb' }}>
        <CardContent sx={{ p: { xs: 2, sm: 3 } }}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1976d2', mb: 2, pl: 1 }}>
            Doctor Balances
          </Typography>
          <TextField
            type="text"
            placeholder="Search doctor balances..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            size="small"
            sx={{ width: { xs: '100%', sm: 260 }, background: '#fff', borderRadius: 1, mb: 2 }}
            InputProps={{ sx: { fontSize: 16 } }}
          />
          {loading && <LoadingSpinner />}
          <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
          <GlobalSnackbar open={!!success} message={success || ''} severity="success" onClose={() => setSuccess(null)} />
          <Box sx={{ mb: 2, display: 'flex', justifyContent: 'flex-end' }}>
            <Button
              variant="contained"
              color="primary"
              disabled={selected.length === 0}
              onClick={async () => {
                setLoading(true);
                setError(null);
                setSuccess(null);
                try {
                  const doctorBalances = sorted
                    .filter(b => selected.includes(getDoctorId(b)))
                    .map(b => ({ doctorId: b.doctor?.doctorId || b.doctor?.id, balance: b.balance }));
                  const res = await apiFetch('/api/doctor-wallet/payout-commission', {
                    method: 'POST',
                    headers: {
                      'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ doctorBalances }),
                  });
                  if (!res.ok) throw new Error('Failed to payout commission');
                  const msg = await res.text();
                  setSuccess(msg);
                  setSelected([]);
                  await fetchBalances();
                } catch (e: any) {
                  setError(e.message || 'Error occurred');
                } finally {
                  setLoading(false);
                }
              }}
            >
              Payout Promotional
            </Button>
          </Box>
          <TableContainer component={Paper} sx={{ mt: 0, width: '100%', boxShadow: 0, borderRadius: 0 }}>
            <Table size="small" sx={{ minWidth: 900 }}>
              <TableHead sx={{ position: 'sticky', top: 0, background: '#f7f7f7', zIndex: 1 }}>
                <TableRow>
                  <TableCell padding="checkbox">
                    <Checkbox
                      indeterminate={selected.length > 0 && selected.length < negativeIds.length}
                      checked={isAllSelected}
                      onChange={handleSelectAll}
                      inputProps={{ 'aria-label': 'select all negative balance doctors' }}
                    />
                  </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '10%' }}>
                    <TableSortLabel
                      active={sortBy === 'doctorId'}
                      direction={sortBy === 'doctorId' ? sortOrder : 'asc'}
                      onClick={() => handleSort('doctorId')}
                    >
                      Doctor ID
                    </TableSortLabel>
                  </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '18%' }}>
                    <TableSortLabel
                      active={sortBy === 'name'}
                      direction={sortBy === 'name' ? sortOrder : 'asc'}
                      onClick={() => handleSort('name')}
                    >
                      Name
                    </TableSortLabel>
                  </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '16%' }}>
                    <TableSortLabel
                      active={sortBy === 'specialization'}
                      direction={sortBy === 'specialization' ? sortOrder : 'asc'}
                      onClick={() => handleSort('specialization')}
                    >
                      Specialization
                    </TableSortLabel>
                  </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '16%' }}>
                    <TableSortLabel
                      active={sortBy === 'phoneNumber'}
                      direction={sortBy === 'phoneNumber' ? sortOrder : 'asc'}
                      onClick={() => handleSort('phoneNumber')}
                    >
                      Phone Number
                    </TableSortLabel>
                  </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '20%' }}>
                    <TableSortLabel
                      active={sortBy === 'email'}
                      direction={sortBy === 'email' ? sortOrder : 'asc'}
                      onClick={() => handleSort('email')}
                    >
                      Email
                    </TableSortLabel>
                  </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '12%' }}>
                    <TableSortLabel
                      active={sortBy === 'balance'}
                      direction={sortBy === 'balance' ? sortOrder : 'asc'}
                      onClick={() => handleSort('balance')}
                    >
                      Balance
                    </TableSortLabel>
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {visibleBalances.map((b: any, idx: number) => {
                  const id = getDoctorId(b);
                  const isNegative = isTrulyNegative(b.balance);
                  return (
                    <TableRow key={id} sx={{ background: idx % 2 === 0 ? '#fff' : '#f9fafb' }}>
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selected.includes(id)}
                          onChange={() => handleSelect(id)}
                          inputProps={{ 'aria-label': `select doctor ${id}` }}
                          disabled={!isNegative}
                        />
                      </TableCell>
                      <TableCell>{id}</TableCell>
                      <TableCell>{b.doctor?.name || ''}</TableCell>
                      <TableCell>{b.doctor?.specialization || ''}</TableCell>
                      <TableCell>{b.doctor?.phoneNumber || ''}</TableCell>
                      <TableCell>{b.doctor?.email || ''}</TableCell>
                      <TableCell>{typeof b.balance === 'number' ? (Math.abs(b.balance) < 1e-8 ? '0.00' : b.balance.toFixed(2)) : b.balance}</TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableContainer>
          <Box sx={{ mt: 2, display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: 'center', justifyContent: 'space-between', gap: 2, px: 1 }}>
            <Typography variant="body2" sx={{ color: '#555' }}>
              Showing {totalBalances === 0 ? 0 : pageStart + 1} - {pageEnd} of {totalBalances} doctors
            </Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems="center" sx={{ width: { xs: '100%', sm: 'auto' } }}>
              <FormControl size="small" sx={{ minWidth: 130, background: '#fff', borderRadius: 1 }}>
                <InputLabel id="rows-per-page-label-balances">Page size</InputLabel>
                <Select
                  labelId="rows-per-page-label-balances"
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
                count={pageCount}
                page={page}
                onChange={(_, value) => setPage(value)}
                color="primary"
                showFirstButton
                showLastButton
                shape="rounded"
              />
            </Stack>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
