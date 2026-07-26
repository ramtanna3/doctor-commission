import { useEffect, useState } from 'react';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import TableSortLabel from '@mui/material/TableSortLabel';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import CheckIcon from '@mui/icons-material/Check';
import CloseIcon from '@mui/icons-material/Close';
import IconButton from '@mui/material/IconButton';
import LoadingSpinner from './components/LoadingSpinner';
import GlobalSnackbar from './components/GlobalSnackbar';
import SuccessDialog from './components/SuccessDialog';
import ConfirmDialog from './components/ConfirmDialog';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Pagination from '@mui/material/Pagination';
import Stack from '@mui/material/Stack';

export type Medical = {
  medicalId?: number;
  name: string;
  address: string;
  phoneNumber: string;
  email: string;
  isActive?: boolean;
};

const API_BASE = '/api/medicals';

export default function MedicalCrud() {

  // Row edit handlers
  const handleRowEdit = (medical: Medical) => {
    setEditingRowId(medical.medicalId!);
    setRowEditForm({ ...medical });
  };

  const handleRowEditChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setRowEditForm(prev => prev ? { ...prev, [name]: value } : prev);
  };

  const handleRowEditCancel = () => {
    setEditingRowId(null);
    setRowEditForm(null);
  };

  const handleRowEditConfirm = async () => {
    if (!rowEditForm) return;
    if (rowEditForm.phoneNumber && rowEditForm.phoneNumber.length !== 10) {
      setError('Phone number must be 10 digits');
      return;
    }
    if (rowEditForm.email && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(rowEditForm.email)) {
      setError('Invalid email address');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE}/${editingRowId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'x-user-id': '1' },
        body: JSON.stringify(rowEditForm),
      });
      if (!res.ok) throw new Error('Failed to update medical');
      setSuccess('Medical updated successfully!');
      setShowSuccessDialog(true);
      setEditingRowId(null);
      setRowEditForm(null);
      fetchMedicals();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  // Sorting handler
  const handleSort = (column: 'name' | 'address' | 'phoneNumber' | 'email') => {
    if (sortBy === column) {
      setSortOrder(prev => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(column);
      setSortOrder('asc');
    }
  };

  // Delete confirmation handler
  const handleConfirmDelete = async () => {
    if (!confirmDeleteId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE}/${confirmDeleteId}`, { method: 'DELETE', headers: { 'x-user-id': '1' } });
      if (!res.ok) throw new Error('Failed to delete');
      setSuccess('Medical deleted successfully!');
      setShowSuccessDialog(true);
      fetchMedicals();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
      setConfirmDeleteId(null);
    }
  };

  const [medicals, setMedicals] = useState<Medical[]>([]);
  const [form, setForm] = useState<Medical>({ name: '', address: '', phoneNumber: '', email: '' });
  const [formErrors, setFormErrors] = useState<{ phoneNumber?: string; email?: string }>({});
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingRowId, setEditingRowId] = useState<number | null>(null);
  const [rowEditForm, setRowEditForm] = useState<Medical | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showSuccessDialog, setShowSuccessDialog] = useState(false);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState<'name' | 'address' | 'phoneNumber' | 'email'>('name');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);
  const [page, setPage] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const fetchMedicals = async () => {
    setLoading(true);
    try {
      const res = await fetch(API_BASE, {
        headers: { 'x-user-id': '1' }
      });
      if (!res.ok) throw new Error('Failed to fetch medicals');
      setMedicals(await res.json());
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchMedicals(); }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    if (name === 'phoneNumber') {
      if (!/^[0-9]*$/.test(value)) return;
      setForm({ ...form, [name]: value });
      setFormErrors(errors => ({
        ...errors,
        phoneNumber: value.length === 0 || value.length === 10 ? undefined : 'Phone number must be 10 digits'
      }));
    } else if (name === 'email') {
      setForm({ ...form, [name]: value });
      setFormErrors(errors => ({
        ...errors,
        email: value.length === 0 || /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(value) ? undefined : 'Invalid email address'
      }));
    } else {
      setForm({ ...form, [name]: value });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (form.phoneNumber && form.phoneNumber.length !== 10) {
      setFormErrors(errors => ({ ...errors, phoneNumber: 'Phone number must be 10 digits' }));
      return;
    }
    if (form.email && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(form.email)) {
      setFormErrors(errors => ({ ...errors, email: 'Invalid email address' }));
      return;
    }
    setLoading(true);
    try {
      const method = editingId ? 'PUT' : 'POST';
      const url = editingId ? `${API_BASE}/${editingId}` : API_BASE;
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json', 'x-user-id': '1' },
        body: JSON.stringify(form),
      });
      if (!res.ok) throw new Error('Failed to save medical');
      setForm({ name: '', address: '', phoneNumber: '', email: '' });
      setEditingId(null);
      const isUpdate = !!editingId;
      setSuccess(isUpdate ? 'Medical updated successfully!' : 'Medical added successfully!');
      setShowSuccessDialog(true);
      if (isUpdate) {
        fetchMedicals();
      } else {
        const newMedical = await res.json();
        setMedicals(prev => [newMedical, ...prev]);
      }
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };


  const filteredMedicals = medicals.filter(m => {
    const s = search.toLowerCase();
    return (
      (m.name || '').toLowerCase().includes(s) ||
      (m.address || '').toLowerCase().includes(s) ||
      (m.phoneNumber || '').toLowerCase().includes(s) ||
      (m.email || '').toLowerCase().includes(s)
    );
  });

  const sortedMedicals = [...filteredMedicals].sort((a, b) => {
    const aVal = (a[sortBy] || '').toLowerCase();
    const bVal = (b[sortBy] || '').toLowerCase();
    if (aVal < bVal) return sortOrder === 'asc' ? -1 : 1;
    if (aVal > bVal) return sortOrder === 'asc' ? 1 : -1;
    return 0;
  });

  const totalMedicals = sortedMedicals.length;
  const pageCount = Math.max(1, Math.ceil(totalMedicals / rowsPerPage));
  const pageStart = (page - 1) * rowsPerPage;
  const pageEnd = Math.min(pageStart + rowsPerPage, totalMedicals);
  const visibleMedicals = sortedMedicals.slice(pageStart, pageEnd);

  useEffect(() => {
    if (page > pageCount) setPage(pageCount);
  }, [page, pageCount]);

  useEffect(() => {
    setPage(1);
  }, [search, rowsPerPage]);

  // --- Modernized UI/UX and features ---
  return (
    <Box sx={{ flex: 1, width: '100%', minHeight: 'calc(100vh - 64px)', background: '#f5f5f5', p: { xs: 1, sm: 1 }, boxSizing: 'border-box', display: 'flex', flexDirection: 'column' }}>
      {/* Add Medical Section */}
      <Card sx={{ width: '100%', maxWidth: 1100, m: '0 auto', mb: 3, boxShadow: 2, borderRadius: 3, background: '#f9fafb' }}>
        <CardContent sx={{ p: { xs: 2, sm: 3 } }}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1976d2', mb: 2, pl: 1 }}>
            Add Medical
          </Typography>
          <form onSubmit={handleSubmit} style={{ width: '100%' }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <TextField
                  name="name"
                  label="Name"
                  value={form.name}
                  onChange={handleChange}
                  required
                  size="small"
                  sx={{ flex: 1, minWidth: 180, background: '#fff', borderRadius: 1 }}
                  FormHelperTextProps={{ sx: { minHeight: 24, background: '#fff', m: 0, p: 0 } }}
                />
                <TextField
                  name="address"
                  label="Address"
                  value={form.address}
                  onChange={handleChange}
                  size="small"
                  sx={{ flex: 1, minWidth: 180, background: '#fff', borderRadius: 1 }}
                  FormHelperTextProps={{ sx: { minHeight: 24, background: '#fff', m: 0, p: 0 } }}
                />
              </Box>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <TextField
                  name="phoneNumber"
                  label="Phone Number"
                  value={form.phoneNumber}
                  onChange={handleChange}
                  size="small"
                  sx={{ flex: 1, minWidth: 180, background: '#fff', borderRadius: 1 }}
                  inputProps={{ maxLength: 10, inputMode: 'numeric', pattern: '[0-9]*' }}
                  error={!!formErrors.phoneNumber}
                  helperText={formErrors.phoneNumber ? formErrors.phoneNumber : ''}
                  FormHelperTextProps={{ sx: { background: '#fff', m: 0, p: 0 } }}
                />
                <TextField
                  name="email"
                  label="Email"
                  value={form.email}
                  onChange={handleChange}
                  size="small"
                  sx={{ flex: 1, minWidth: 180, background: '#fff', borderRadius: 1 }}
                  type="email"
                  error={!!formErrors.email}
                  helperText={formErrors.email ? formErrors.email : ''}
                  FormHelperTextProps={{ sx: { background: '#fff', m: 0, p: 0 } }}
                />
              </Box>
              <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-start', mt: 0.5 }}>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  disabled={loading}
                  sx={{ minWidth: 140, height: 40, whiteSpace: 'nowrap', fontWeight: 600, fontSize: 16, px: 2, boxShadow: 1 }}
                >
                  {editingId ? 'Update' : 'Add'} Medical
                </Button>
                {editingId && (
                  <Button
                    type="button"
                    variant="outlined"
                    color="secondary"
                    onClick={() => { setEditingId(null); setForm({ name: '', address: '', phoneNumber: '', email: '' }); }}
                    sx={{ minWidth: 100, height: 40, whiteSpace: 'nowrap', fontWeight: 600, fontSize: 16, px: 2 }}
                  >
                    Cancel
                  </Button>
                )}
              </Box>
            </Box>
          </form>
        </CardContent>
      </Card>
      {/* Medicals Section: Filters + Table merged */}
      <Card sx={{ width: '100%', maxWidth: 1100, m: '0 auto', boxShadow: 1, borderRadius: 2, flex: 1, display: 'flex', flexDirection: 'column' }}>
        <CardContent sx={{ p: 0 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', px: 3, py: 2, borderBottom: '1px solid #eee', background: '#f7f7f7', borderTopLeftRadius: 8, borderTopRightRadius: 8, gap: 2, flexWrap: 'wrap' }}>
            <Typography variant="h6" sx={{ flex: 1, fontWeight: 700, color: '#222' }}>
              Medicals
            </Typography>
            <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#555', pr: 2 }}>
              Filters
            </Typography>
            <TextField
              type="text"
              placeholder="Search medicals..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              size="small"
              sx={{ width: { xs: '100%', sm: 320 }, background: '#fff', borderRadius: 1 }}
              InputProps={{ sx: { fontSize: 16 } }}
            />
          </Box>
          <Box sx={{ p: { xs: 1, sm: 3 }, pt: 2 }}>
            {loading && <LoadingSpinner />}
            <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
            <SuccessDialog open={showSuccessDialog} message={success || ''} onClose={() => setShowSuccessDialog(false)} />
            <TableContainer component={Paper} sx={{ mt: 0, width: '100%', boxShadow: 0, borderRadius: 0 }}>
              <Table size="small" sx={{ minWidth: 650 }}>
                <TableHead sx={{ position: 'sticky', top: 0, background: '#f7f7f7', zIndex: 1 }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 700, width: '52%' }}>
                      <TableSortLabel
                        active={sortBy === 'name'}
                        direction={sortBy === 'name' ? sortOrder : 'asc'}
                        onClick={() => handleSort('name')}
                      >
                        Name
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '12%' }}>
                      <TableSortLabel
                        active={sortBy === 'address'}
                        direction={sortBy === 'address' ? sortOrder : 'asc'}
                        onClick={() => handleSort('address')}
                      >
                        Address
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '12%' }}>
                      <TableSortLabel
                        active={sortBy === 'phoneNumber'}
                        direction={sortBy === 'phoneNumber' ? sortOrder : 'asc'}
                        onClick={() => handleSort('phoneNumber')}
                      >
                        Phone
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '12%' }}>
                      <TableSortLabel
                        active={sortBy === 'email'}
                        direction={sortBy === 'email' ? sortOrder : 'asc'}
                        onClick={() => handleSort('email')}
                      >
                        Email
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '12%' }}>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {visibleMedicals.length > 0 ? visibleMedicals.map((m, idx) => (
                    <TableRow key={m.medicalId} sx={{ background: idx % 2 === 0 ? '#fff' : '#f9fafb' }}>
                      {editingRowId === m.medicalId ? (
                        <>
                          <TableCell sx={{ width: '52%' }}>
                            <TextField name="name" value={rowEditForm?.name || ''} onChange={handleRowEditChange} size="small" sx={{ width: '100%' }} />
                          </TableCell>
                          <TableCell sx={{ width: '12%' }}>
                            <TextField name="address" value={rowEditForm?.address || ''} onChange={handleRowEditChange} size="small" sx={{ width: '100%' }} />
                          </TableCell>
                          <TableCell sx={{ width: '12%' }}>
                            <TextField name="phoneNumber" value={rowEditForm?.phoneNumber || ''} onChange={handleRowEditChange} size="small" sx={{ width: '100%' }} />
                          </TableCell>
                          <TableCell sx={{ width: '12%' }}>
                            <TextField name="email" value={rowEditForm?.email || ''} onChange={handleRowEditChange} size="small" sx={{ width: '100%' }} />
                          </TableCell>
                          <TableCell sx={{ width: '12%' }}>
                            <IconButton color="success" onClick={handleRowEditConfirm} size="small"><CheckIcon /></IconButton>
                            <IconButton color="inherit" onClick={handleRowEditCancel} size="small"><CloseIcon /></IconButton>
                          </TableCell>
                        </>
                      ) : (
                        <>
                          <TableCell sx={{ width: '52%' }}>{m.name}</TableCell>
                          <TableCell sx={{ width: '12%' }}>{m.address}</TableCell>
                          <TableCell sx={{ width: '12%' }}>{m.phoneNumber}</TableCell>
                          <TableCell sx={{ width: '12%' }}>{m.email}</TableCell>
                          <TableCell sx={{ width: '12%' }}>
                            <IconButton color="primary" onClick={() => handleRowEdit(m)} size="small"><EditIcon /></IconButton>
                            <IconButton color="error" onClick={() => setConfirmDeleteId(m.medicalId!)} size="small" sx={{ ml: 1 }}><DeleteIcon /></IconButton>
                          </TableCell>
                        </>
                      )}
                    </TableRow>
                  )) : (
                    <TableRow>
                      <TableCell colSpan={5} sx={{ textAlign: 'center', py: 4, color: '#666' }}>
                        No medicals match your search.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
            <Box sx={{ mt: 2, display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: 'center', justifyContent: 'space-between', gap: 2, px: 1 }}>
              <Typography variant="body2" sx={{ color: '#555' }}>
                Showing {totalMedicals === 0 ? 0 : pageStart + 1} - {pageEnd} of {totalMedicals} medicals
              </Typography>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems="center" sx={{ width: { xs: '100%', sm: 'auto' } }}>
                <FormControl size="small" sx={{ minWidth: 130, background: '#fff', borderRadius: 1 }}>
                  <InputLabel id="rows-per-page-label-medical">Page size</InputLabel>
                  <Select
                    labelId="rows-per-page-label-medical"
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
          </Box>
        </CardContent>
      </Card>
      <ConfirmDialog
        open={!!confirmDeleteId}
        title="Confirm Delete"
        message="Are you sure you want to delete this medical?"
        onClose={confirmed => {
          if (confirmed) handleConfirmDelete();
          else setConfirmDeleteId(null);
        }}
      />
    </Box>
  );
}
