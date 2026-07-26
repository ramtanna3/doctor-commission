import { useEffect, useMemo, useState } from 'react';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import IconButton from '@mui/material/IconButton';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import LoadingSpinner from './components/LoadingSpinner';
import GlobalSnackbar from './components/GlobalSnackbar';
import SuccessDialog from './components/SuccessDialog';
import Box from '@mui/material/Box';
import TableSortLabel from '@mui/material/TableSortLabel';
import ConfirmDialog from './components/ConfirmDialog';

import Checkbox from '@mui/material/Checkbox';
import Button from '@mui/material/Button';
import Autocomplete from '@mui/material/Autocomplete';
import MenuItem from '@mui/material/MenuItem';
import Chip from '@mui/material/Chip';
import { buildVisibleTransactionsForExport, exportTransactionsToExcel, exportTransactionsToPdf } from './utils/exportData';

import Select from '@mui/material/Select';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Stack from '@mui/material/Stack';
import Pagination from '@mui/material/Pagination';

export default function SalesTransactions() {
  const [distributors, setDistributors] = useState<any[]>([]);
  const [selectedDistributor, setSelectedDistributor] = useState<string>('');
  const [transactions, setTransactions] = useState<any[]>([]);
  const [distributorName, setDistributorName] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showSuccessDialog, setShowSuccessDialog] = useState(false);
  const [search, setSearch] = useState('');
  // Status filter for Matched/Unmatched
  const statusOptions = [
    { label: 'Matched', value: 'matched' },
    { label: 'Unmatched', value: 'unmatched' }
  ];
  const [statusFilter, setStatusFilter] = useState(['matched', 'unmatched']);
  const [dateFrom, setDateFrom] = useState<string>('');
  const [dateTo, setDateTo] = useState<string>('');
  const [sortBy, setSortBy] = useState<string>('date');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [doctors, setDoctors] = useState<any[]>([]);
  const [selectedTransactionIds, setSelectedTransactionIds] = useState<number[]>([]);
  const [bulkDoctor, setBulkDoctor] = useState<any>(null);
  const [confirmDialogId, setConfirmDialogId] = useState<string | null>(null);
  const [page, setPage] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  useEffect(() => {
    fetchDistributors();
    fetchDoctors();
  }, []);

  useEffect(() => {
    if (selectedDistributor) fetchTransactions(selectedDistributor);
    // Do not clear transactions immediately; keep previous data until new data is fetched
    // This prevents table flicker and jump
  }, [selectedDistributor]);

  const fetchDistributors = async () => {
  const res = await fetch('/api/distributors', { headers: { 'x-user-id': '1' } });
    if (res.ok) setDistributors(await res.json());
  };

  const fetchTransactions = async (distributorId: string) => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`/api/sales-transactions/by-distributor/${distributorId}/all`, { headers: { 'x-user-id': '1' } });
      if (!res.ok) throw new Error('Failed to fetch sales transactions');
      const data = await res.json();
      setTransactions(data.transactions || []);
      setDistributorName(data.distributor?.distributorName || '');
      setSelectedTransactionIds([]);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const fetchDoctors = async () => {
    try {
      const res = await fetch('/api/doctors/active', { headers: { 'x-user-id': '1' } });
      if (!res.ok) throw new Error('Failed to fetch doctors');
      const data = await res.json();
      setDoctors(data || []);
    } catch (e: any) {
      setError(e.message);
    }
  };

  const getDoctorId = (doctor: any) => doctor?.doctorId ?? doctor?.id ?? null;

  const assignDoctor = async (salesTransactionIds: number[], doctor: any) => {
    if (!doctor || !salesTransactionIds.length) return;
    setLoading(true);
    setError(null);
    try {
      const res = await fetch('/api/sales-transactions/assign-doctor', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'x-user-id': '1'
        },
        body: JSON.stringify({
          salesTransactionIds,
          doctorId: getDoctorId(doctor)
        })
      });
      if (!res.ok) {
        const errorBody = await res.text();
        throw new Error(errorBody || 'Failed to assign doctor');
      }
      const result = await res.json();
      setSuccess(`Assigned ${result.assignedSalesTransactionIds?.length || 0} transaction(s) to ${doctor.name || 'doctor'}`);
      setShowSuccessDialog(true);
      setSelectedTransactionIds(prev => prev.filter(id => !salesTransactionIds.includes(id)));
      fetchTransactions(selectedDistributor);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleAssignDoctorToSelected = async () => {
    await assignDoctor(selectedTransactionIds, bulkDoctor);
    setBulkDoctor(null);
  };

  const handleTransactionCheckboxChange = (transactionId: number, checked: boolean) => {
    setSelectedTransactionIds(prev => {
      if (checked) {
        return [...prev, transactionId];
      }
      return prev.filter(id => id !== transactionId);
    });
  };

  const visibleTransactions = useMemo(() => buildVisibleTransactionsForExport(transactions, {
    search,
    statusFilter,
    dateFrom,
    dateTo,
  }), [transactions, search, statusFilter, dateFrom, dateTo]);

  const visibleUnmatchedTransactions = useMemo(
    () => visibleTransactions.filter((t: any) => t.isMatched === false),
    [visibleTransactions]
  );

  const visibleUnmatchedTransactionIds = useMemo(
    () => visibleUnmatchedTransactions.map((t: any) => t.salesTransactionId),
    [visibleUnmatchedTransactions]
  );

  const isAllVisibleSelected =
    visibleUnmatchedTransactionIds.length > 0 &&
    visibleUnmatchedTransactionIds.every(id => selectedTransactionIds.includes(id));

  const isSomeVisibleSelected =
    selectedTransactionIds.length > 0 &&
    visibleUnmatchedTransactionIds.some(id => selectedTransactionIds.includes(id));

  const handleSelectAll = (checked: boolean) => {
    setSelectedTransactionIds(checked ? visibleUnmatchedTransactionIds : []);
  };

  const sortedTransactions = useMemo(() => {
    return [...visibleTransactions].sort((a: any, b: any) => {
      let aVal = a[sortBy];
      let bVal = b[sortBy];
      if (sortBy === 'doctor') {
        aVal = a.doctor?.name || '';
        bVal = b.doctor?.name || '';
      } else if (sortBy === 'medical') {
        aVal = a.medical?.name || '';
        bVal = b.medical?.name || '';
      } else if (sortBy === 'product') {
        aVal = a.product?.name || '';
        bVal = b.product?.name || '';
      } else if (sortBy === 'date') {
        aVal = a.date || a.transactionDate || '';
        bVal = b.date || b.transactionDate || '';
      }
      if (aVal == null) aVal = '';
      if (bVal == null) bVal = '';
      if (typeof aVal === 'string') aVal = aVal.toLowerCase();
      if (typeof bVal === 'string') bVal = bVal.toLowerCase();
      if (aVal < bVal) return sortOrder === 'asc' ? -1 : 1;
      if (aVal > bVal) return sortOrder === 'asc' ? 1 : -1;
      return 0;
    });
  }, [visibleTransactions, sortBy, sortOrder]);

  const totalTransactions = sortedTransactions.length;
  const pageCount = Math.max(1, Math.ceil(totalTransactions / rowsPerPage));
  const pageStart = (page - 1) * rowsPerPage;
  const pageEnd = Math.min(pageStart + rowsPerPage, totalTransactions);
  const pagedTransactions = sortedTransactions.slice(pageStart, pageEnd);

  useEffect(() => {
    if (page > pageCount) setPage(pageCount);
  }, [page, pageCount]);

  useEffect(() => {
    setPage(1);
  }, [search, statusFilter, dateFrom, dateTo, rowsPerPage, selectedDistributor]);

  const handleSort = (column: string) => {
    if (sortBy === column) {
      setSortOrder(prev => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(column);
      setSortOrder('asc');
    }
  };

  const handleExportExcel = () => {
    if (!selectedDistributor) return;
    exportTransactionsToExcel(visibleTransactions, distributorName || 'sales-transactions');
    setSuccess('Excel export started.');
    setShowSuccessDialog(true);
  };

  const handleExportPdf = () => {
    if (!selectedDistributor) return;
    exportTransactionsToPdf(visibleTransactions, distributorName || 'sales-transactions');
    setSuccess('PDF export started.');
    setShowSuccessDialog(true);
  };

  // No row edit/delete handlers needed

  const handleConfirmDelete = async () => {
    if (!confirmDialogId) return;
    setLoading(true);
    setError(null);
    try {
      // TODO: Replace with actual delete API endpoint
      // const res = await fetch(`/api/sales-transactions/${confirmDialogId}`, { method: 'DELETE', ... });
      setSuccess('Transaction deleted successfully!');
      setShowSuccessDialog(true);
      fetchTransactions(selectedDistributor);
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
      setConfirmDialogId(null);
    }
  };

  return (
    <>
      <Box sx={{ flex: 1, width: '100%', minHeight: 'calc(100vh - 64px)', background: '#f5f5f5', p: { xs: 1, sm: 1 }, boxSizing: 'border-box', display: 'flex', flexDirection: 'column' }}>
        <Card sx={{ width: '100%', maxWidth: 1100, m: '0 auto', mb: 3, boxShadow: 2, borderRadius: 3, background: '#f9fafb' }}>
          <CardContent sx={{ p: { xs: 2, sm: 3 } }}>
            <Typography variant="h6" sx={{ fontWeight: 700, color: '#1976d2', mb: 2, pl: 1 }}>
              Sales Transactions
            </Typography>
            <Autocomplete
            getOptionKey={option => option.id || option.distributorId}
              options={distributors}
              getOptionLabel={(d: any) => d?.name || d?.distributorName || `Distributor #${d?.id || d?.distributorId}`}
              value={distributors.find((d: any) => (d.id || d.distributorId) === selectedDistributor) || null}
              onChange={(_e: any, newValue: any) => setSelectedDistributor(newValue ? (newValue.id || newValue.distributorId) : '')}
              isOptionEqualToValue={(option: any, value: any) => (option.id || option.distributorId) === (value.id || value.distributorId)}
              filterOptions={(options, { inputValue }) =>
                options.filter((d: any) => ((d?.name || d?.distributorName || '').toLowerCase().includes(inputValue.trim().toLowerCase())))
              }
              renderInput={(params: any) => (
                <TextField {...params} label="Select Distributor" size="small" required sx={{ minWidth: 220, maxWidth: 340, mb: 2, background: '#fff', borderRadius: 1 }} />
              )}
              sx={{ minWidth: 220, maxWidth: 340, mb: 2 }}
              disableClearable
            />
          </CardContent>
        </Card>
        <Card sx={{ width: '100%', maxWidth: 1100, m: '0 auto', boxShadow: 1, borderRadius: 2, flex: 1, display: 'flex', flexDirection: 'column' }}>
          <CardContent sx={{ p: 0 }}>
            <Box sx={{ pl: 3, pr: 0, pt: 2, pb: 1, borderBottom: '1px solid #eee', background: '#f7f7f7', borderTopLeftRadius: 8, borderTopRightRadius: 8 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', mb: 1, pr: 0 }}>
                {/* No duplicate header here, matches DoctorCrud */}
                {distributorName && (
                  <Typography
                    variant="subtitle1"
                    sx={{ fontWeight: 600, color: '#333', py: 0.5, borderRadius: 2, fontSize: 16, minWidth: 60 }}
                  >
                    Transactions: <span style={{ color: '#1976d2' }}>{distributorName}</span>
                  </Typography>
                )}
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'nowrap', width: '100%' }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#555', minWidth: 60 }}>
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
                {/* Status filter multi-select with chips */}
                <TextField
                  select
                  label="Status"
                  value={statusFilter}
                  onChange={e => {
                    const value = e.target.value;
                    let newValue = typeof value === 'string' ? value.split(',') : value;
                    // If none selected, keep empty array (show nothing)
                    setStatusFilter(newValue);
                  }}
                  size="small"
                  sx={{ minWidth: 180, maxWidth: 320, background: '#fff', borderRadius: 1 }}
                  SelectProps={{
                    multiple: true,
                    renderValue: (selected: unknown) => {
                      const selectedArr = selected as string[];
                      return (
                        <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                          {selectedArr.map(val => {
                            const opt = statusOptions.find(o => o.value === val);
                            return (
                              <Chip key={val} label={opt?.label || val} size="small" sx={{ fontWeight: 500, background: '#e0e0e0' }} />
                            );
                          })}
                        </Box>
                      );
                    },
                    displayEmpty: true,
                  }}
                >
                  {statusOptions.map(option => (
                    <MenuItem key={option.value} value={option.value}>
                      <input
                        type="checkbox"
                        checked={statusFilter.indexOf(option.value) > -1}
                        readOnly
                        style={{ marginRight: 8 }}
                      />
                      {option.label}
                    </MenuItem>
                  ))}
                </TextField>
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
                <Box sx={{ ml: 'auto', display: 'flex', alignItems: 'center', gap: 0.5 }}>
                  <IconButton
                    onClick={handleExportExcel}
                    disabled={!selectedDistributor || visibleTransactions.length === 0}
                    aria-label="Export Excel"
                    title="Export Excel"
                    sx={{ p: 0.5 }}
                  >
                    <img src="/excel2-svgrepo-com.svg" alt="Excel" style={{ width: 22, height: 22, display: 'block' }} />
                  </IconButton>
                  <IconButton
                    onClick={handleExportPdf}
                    disabled={!selectedDistributor || visibleTransactions.length === 0}
                    aria-label="Export PDF"
                    title="Export PDF"
                    sx={{ p: 0.5 }}
                  >
                    <img src="/pdf-file-svgrepo-com.svg" alt="PDF" style={{ width: 22, height: 22, display: 'block' }} />
                  </IconButton>
                </Box>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap', mt: 1, px: 1 }}>
                <Autocomplete
                  options={doctors}
                  getOptionLabel={(d: any) => d?.name || d?.doctorName || ''}
                  value={bulkDoctor}
                  onChange={(_e: any, newValue: any) => setBulkDoctor(newValue)}
                  isOptionEqualToValue={(option: any, value: any) => (option.doctorId || option.id) === (value?.doctorId || value?.id)}
                  renderInput={(params: any) => (
                    <TextField {...params} label="Doctor for selected" size="small" sx={{ minWidth: 220, background: '#fff', borderRadius: 1 }} />
                  )}
                  sx={{ minWidth: 220, maxWidth: 320, background: '#fff', borderRadius: 1 }}
                />
                <Button
                  variant="contained"
                  color="primary"
                  disabled={!bulkDoctor || selectedTransactionIds.length === 0 || loading}
                  onClick={handleAssignDoctorToSelected}
                  sx={{ height: 36 }}
                >
                  Assign to selected ({selectedTransactionIds.length})
                </Button>
              </Box>
            </Box>
            <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
            <SuccessDialog open={showSuccessDialog} message={success || ''} onClose={() => setShowSuccessDialog(false)} />
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
                    <TableCell sx={{ fontWeight: 700, width: '4%', color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                      <Checkbox
                        size="small"
                        indeterminate={isSomeVisibleSelected && !isAllVisibleSelected}
                        checked={isAllVisibleSelected}
                        disabled={visibleUnmatchedTransactionIds.length === 0}
                        onChange={e => handleSelectAll(e.target.checked)}
                      />
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '10%', color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                      <TableSortLabel
                        active={sortBy === 'date'}
                        direction={sortBy === 'date' ? sortOrder : 'asc'}
                        onClick={() => handleSort('date')}
                        classes={{ root: 'MuiTableSortLabel-root', active: 'Mui-active', icon: 'MuiTableSortLabel-icon', iconDirectionAsc: 'MuiTableSortLabel-directionAsc' }}
                      >
                        Date
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '10%', color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                      Voucher ID
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '12%', color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                      <TableSortLabel
                        active={sortBy === 'doctor'}
                        direction={sortBy === 'doctor' ? sortOrder : 'asc'}
                        onClick={() => handleSort('doctor')}
                        classes={{ root: 'MuiTableSortLabel-root', active: 'Mui-active', icon: 'MuiTableSortLabel-icon', iconDirectionAsc: 'MuiTableSortLabel-directionAsc' }}
                      >
                        Doctor
                      </TableSortLabel>
                    </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '12%', color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                    <TableSortLabel
                      active={sortBy === 'medical'}
                      direction={sortBy === 'medical' ? sortOrder : 'asc'}
                      onClick={() => handleSort('medical')}
                      classes={{ root: 'MuiTableSortLabel-root', active: 'Mui-active', icon: 'MuiTableSortLabel-icon', iconDirectionAsc: 'MuiTableSortLabel-directionAsc' }}
                    >
                      Medical
                    </TableSortLabel>
                  </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '12%', color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                    <TableSortLabel
                      active={sortBy === 'product'}
                      direction={sortBy === 'product' ? sortOrder : 'asc'}
                      onClick={() => handleSort('product')}
                      classes={{ root: 'MuiTableSortLabel-root', active: 'Mui-active', icon: 'MuiTableSortLabel-icon', iconDirectionAsc: 'MuiTableSortLabel-directionAsc' }}
                    >
                      Product
                    </TableSortLabel>
                  </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '8%', color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                    <TableSortLabel
                      active={sortBy === 'quantity'}
                      direction={sortBy === 'quantity' ? sortOrder : 'asc'}
                      onClick={() => handleSort('quantity')}
                      classes={{ root: 'MuiTableSortLabel-root', active: 'Mui-active', icon: 'MuiTableSortLabel-icon', iconDirectionAsc: 'MuiTableSortLabel-directionAsc' }}
                    >
                      Quantity
                    </TableSortLabel>
                  </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '10%', color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                    <TableSortLabel
                      active={sortBy === 'amount'}
                      direction={sortBy === 'amount' ? sortOrder : 'asc'}
                      onClick={() => handleSort('amount')}
                      classes={{ root: 'MuiTableSortLabel-root', active: 'Mui-active', icon: 'MuiTableSortLabel-icon', iconDirectionAsc: 'MuiTableSortLabel-directionAsc' }}
                    >
                      Amount
                    </TableSortLabel>
                  </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '8%', color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                    <TableSortLabel
                      active={sortBy === 'commissionPercent'}
                      direction={sortBy === 'commissionPercent' ? sortOrder : 'asc'}
                      onClick={() => handleSort('commissionPercent')}
                      classes={{ root: 'MuiTableSortLabel-root', active: 'Mui-active', icon: 'MuiTableSortLabel-icon', iconDirectionAsc: 'MuiTableSortLabel-directionAsc' }}
                    >
                      Promotional %
                    </TableSortLabel>
                  </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '10%', color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                    <TableSortLabel
                      active={sortBy === 'commissionAmount'}
                      direction={sortBy === 'commissionAmount' ? sortOrder : 'asc'}
                      onClick={() => handleSort('commissionAmount')}
                      classes={{ root: 'MuiTableSortLabel-root', active: 'Mui-active', icon: 'MuiTableSortLabel-icon', iconDirectionAsc: 'MuiTableSortLabel-directionAsc' }}
                    >
                      Promotional Amount
                    </TableSortLabel>
                  </TableCell>
                  <TableCell sx={{ fontWeight: 700, width: '8%', color: '#222', background: '#f7f7f7', fontSize: 15 }}>
                    <TableSortLabel
                      active={sortBy === 'status'}
                      direction={sortBy === 'status' ? sortOrder : 'asc'}
                      onClick={() => handleSort('status')}
                      classes={{ root: 'MuiTableSortLabel-root', active: 'Mui-active', icon: 'MuiTableSortLabel-icon', iconDirectionAsc: 'MuiTableSortLabel-directionAsc' }}
                    >
                      Status
                    </TableSortLabel>
                  </TableCell>
                  {/* No Actions column */}
                </TableRow>
              </TableHead>
              <TableBody>
                {pagedTransactions.map((t: any, idx: number) => (
                    <TableRow key={t.salesTransactionId || t.transactionId || t.id} sx={{ background: idx % 2 === 0 ? '#fff' : '#f9fafb' }}>
                      <TableCell sx={{ width: '4%' }}>
                        <Checkbox
                          size="small"
                          checked={selectedTransactionIds.includes(t.salesTransactionId)}
                          disabled={t.isMatched !== false}
                          onChange={e => handleTransactionCheckboxChange(t.salesTransactionId, e.target.checked)}
                        />
                      </TableCell>
                      <TableCell sx={{ width: '10%' }}>{t.date || t.transactionDate || ''}</TableCell>
                      <TableCell sx={{ width: '10%' }}>{t.voucherNo || ''}</TableCell>
                      <TableCell sx={{ width: '20%' }}>{t.doctor?.name || ''}</TableCell>
                      <TableCell sx={{ width: '12%' }}>{t.medical?.name || ''}</TableCell>
                      <TableCell sx={{ width: '12%' }}>{t.product?.name || ''}</TableCell>
                      <TableCell sx={{ width: '8%' }}>{t.qty || t.quantity}</TableCell>
                      <TableCell sx={{ width: '10%' }}>{t.amount}</TableCell>
                      <TableCell sx={{ width: '8%' }}>{t.commissionPercent != null ? t.commissionPercent : ''}</TableCell>
                      <TableCell sx={{ width: '10%' }}>{t.commissionAmount != null ? t.commissionAmount : ''}</TableCell>
                      <TableCell sx={{ width: '8%' }}>{t.status || (t.isMatched === false ? 'Unmatched' : 'Matched')}</TableCell>
                    </TableRow>
                  ))}
              </TableBody>
            </Table>
          </TableContainer>
          <Box sx={{ mt: 2, display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: 'center', justifyContent: 'space-between', gap: 2, px: 1 }}>
            <Typography variant="body2" sx={{ color: '#555' }}>
              Showing {totalTransactions === 0 ? 0 : pageStart + 1} - {pageEnd} of {totalTransactions} transactions
            </Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems="center" sx={{ width: { xs: '100%', sm: 'auto' } }}>
              <FormControl size="small" sx={{ minWidth: 130, background: '#fff', borderRadius: 1 }}>
                <InputLabel id="rows-per-page-label-sales">Page size</InputLabel>
                <Select
                  labelId="rows-per-page-label-sales"
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
        </Box> {/* Close Box with position: 'relative' */}
      </CardContent>
      </Card>
    </Box>
    <ConfirmDialog
        open={!!confirmDialogId}
        title="Confirm Delete"
        message="Are you sure you want to delete this transaction?"
        onClose={confirmed => {
          if (confirmed) handleConfirmDelete();
          else setConfirmDialogId(null);
        }}
      />
    </>
  );
}
