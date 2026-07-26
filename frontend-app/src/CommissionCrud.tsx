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
import ConfirmDialog from './components/ConfirmDialog';
import Autocomplete from '@mui/material/Autocomplete';
import Tooltip from '@mui/material/Tooltip';
import Select from '@mui/material/Select';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import Pagination from '@mui/material/Pagination';
import Stack from '@mui/material/Stack';
import MenuItem from '@mui/material/MenuItem';

export type CommissionMaster = {
  commissionId?: number;
  doctorId?: number;
  medicalId?: number;
  productId?: number;
  doctor?: { doctorId: number; name: string };
  medical?: { medicalId: number; name: string };
  product?: { productId: number; name: string };
  commissionPercentage: number;
  isActive?: boolean;
};

const API_BASE = '/api/commissions';

export default function CommissionCrud() {
  const [commissions, setCommissions] = useState<CommissionMaster[]>([]);
  const [form, setForm] = useState<CommissionMaster>({ commissionPercentage: 0 });
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingRowId, setEditingRowId] = useState<number | null>(null);
  const [rowEditForm, setRowEditForm] = useState<CommissionMaster | null>(null);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState<'doctor' | 'medical' | 'product' | 'commissionPercentage'>('doctor');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);
  const [page, setPage] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showSuccessDialog, setShowSuccessDialog] = useState(false);
  const [loading, setLoading] = useState(false);
  const [doctors, setDoctors] = useState<any[]>([]);
  const [medicals, setMedicals] = useState<any[]>([]);
  const [products, setProducts] = useState<any[]>([]);

  useEffect(() => {
    fetchDoctors();
    fetchMedicals();
    fetchProducts();
    fetchCommissions();
  }, []);

  const fetchDoctors = async () => {
    const res = await fetch('/api/doctors', { headers: { 'x-user-id': '1' } });
    setDoctors(await res.json());
  };
  const fetchMedicals = async () => {
    const res = await fetch('/api/medicals', { headers: { 'x-user-id': '1' } });
    setMedicals(await res.json());
  };
  const fetchProducts = async () => {
    const res = await fetch('/api/products', { headers: { 'x-user-id': '1' } });
    setProducts(await res.json());
  };
  const fetchCommissions = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/active`, { headers: { 'x-user-id': '1' } });
      if (!res.ok) throw new Error('Failed to fetch promotionals');
      setCommissions(await res.json());
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement> | any) => {
    const target = e.target;
    const name = target.name;
    const value = target.value;
    if (name === 'doctorId' || name === 'medicalId' || name === 'productId') {
      setForm({ ...form, [name]: value === '' ? undefined : Number(value) });
    } else {
      setForm({ ...form, [name]: value });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    // Prevent duplicate (doctor, product, medical)
    const exists = commissions.some(c =>
      (c.doctorId ?? c.doctor?.doctorId) == form.doctorId &&
      (c.productId ?? c.product?.productId) == form.productId &&
      (c.medicalId ?? c.medical?.medicalId) == form.medicalId &&
      (editingId == null || c.commissionId !== editingId)
    );
    if (exists) {
      setError('Duplicate entry for Doctor, Product, Medical.');
      setLoading(false);
      return;
    }
    try {
      const method = editingId ? 'PUT' : 'POST';
      const url = editingId ? `${API_BASE}/${editingId}` : API_BASE;
      // Send nested objects as required by backend
      const payload = {
        commissionPercentage: form.commissionPercentage,
        doctor: form.doctorId ? { doctorId: form.doctorId } : undefined,
        medical: form.medicalId ? { medicalId: form.medicalId } : undefined,
        product: form.productId ? { productId: form.productId } : undefined,
        isActive: form.isActive
      };
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json', 'x-user-id': '1' },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error('Failed to save promotional');
      setForm({ commissionPercentage: 0 });
      setEditingId(null);
      setSuccess(editingId ? 'Promotional updated successfully!' : 'Promotional added successfully!');
      setShowSuccessDialog(true);
      fetchCommissions();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleRowEdit = (c: CommissionMaster) => {
    setEditingRowId(c.commissionId!);
    setRowEditForm({
      doctorId: c.doctorId ?? c.doctor?.doctorId,
      medicalId: c.medicalId ?? c.medical?.medicalId,
      productId: c.productId ?? c.product?.productId,
      commissionPercentage: c.commissionPercentage,
      isActive: c.isActive,
    });
  };

  const handleRowEditChange = (e: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>) => {
    const { name, value } = e.target;
    setRowEditForm(prev => prev ? { ...prev, [name!]: value } : prev);
  };

  const handleRowEditConfirm = async () => {
    if (!rowEditForm) return;
    setLoading(true);
    setError(null);
    try {
      const payload = {
        commissionPercentage: rowEditForm.commissionPercentage,
        doctor: rowEditForm.doctorId ? { doctorId: rowEditForm.doctorId } : undefined,
        medical: rowEditForm.medicalId ? { medicalId: rowEditForm.medicalId } : undefined,
        product: rowEditForm.productId ? { productId: rowEditForm.productId } : undefined,
        isActive: rowEditForm.isActive
      };
      const res = await fetch(`${API_BASE}/${editingRowId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'x-user-id': '1' },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error('Failed to update promotional');
      setSuccess('Promotional updated successfully!');
      setEditingRowId(null);
      setRowEditForm(null);
      fetchCommissions();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleRowEditCancel = () => {
    setEditingRowId(null);
    setRowEditForm(null);
  };

  const handleDelete = (id: number) => {
    setConfirmDeleteId(id);
  };

  const handleConfirmDelete = async () => {
    if (!confirmDeleteId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE}/${confirmDeleteId}`, { method: 'DELETE', headers: { 'x-user-id': '1' } });
      if (!res.ok) throw new Error('Failed to delete');
      setSuccess('Promotional deleted successfully!');
      fetchCommissions();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
      setConfirmDeleteId(null);
    }
  };

  const handleSort = (column: 'doctor' | 'medical' | 'product' | 'commissionPercentage') => {
    if (sortBy === column) {
      setSortOrder(prev => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(column);
      setSortOrder('asc');
    }
  };

  const filteredCommissions = commissions.filter(c => {
    const s = search.toLowerCase();
    return (
      (c.doctor?.name || doctors.find(d => d.doctorId === (c.doctorId ?? (c.doctor as any)?.doctorId))?.name || '').toLowerCase().includes(s) ||
      (c.medical?.name || medicals.find(m => m.medicalId === (c.medicalId ?? (c.medical as any)?.medicalId))?.name || '').toLowerCase().includes(s) ||
      (c.product?.name || products.find(p => p.productId === (c.productId ?? (c.product as any)?.productId))?.name || '').toLowerCase().includes(s) ||
      (c.commissionPercentage !== undefined && String(c.commissionPercentage).includes(s))
    );
  });

  const sortedCommissions = [...filteredCommissions].sort((a, b) => {
    let aVal = '', bVal = '';
    if (sortBy === 'doctor') {
      aVal = (a.doctor?.name || doctors.find(d => d.doctorId === (a.doctorId ?? (a.doctor as any)?.doctorId))?.name || '').toLowerCase();
      bVal = (b.doctor?.name || doctors.find(d => d.doctorId === (b.doctorId ?? (b.doctor as any)?.doctorId))?.name || '').toLowerCase();
    } else if (sortBy === 'medical') {
      aVal = (a.medical?.name || medicals.find(m => m.medicalId === (a.medicalId ?? (a.medical as any)?.medicalId))?.name || '').toLowerCase();
      bVal = (b.medical?.name || medicals.find(m => m.medicalId === (b.medicalId ?? (b.medical as any)?.medicalId))?.name || '').toLowerCase();
    } else if (sortBy === 'product') {
      aVal = (a.product?.name || products.find(p => p.productId === (a.productId ?? (a.product as any)?.productId))?.name || '').toLowerCase();
      bVal = (b.product?.name || products.find(p => p.productId === (b.productId ?? (b.product as any)?.productId))?.name || '').toLowerCase();
    } else if (sortBy === 'commissionPercentage') {
      aVal = String(a.commissionPercentage);
      bVal = String(b.commissionPercentage);
    }
    if (aVal < bVal) return sortOrder === 'asc' ? -1 : 1;
    if (aVal > bVal) return sortOrder === 'asc' ? 1 : -1;
    return 0;
  });

  const totalCommissions = sortedCommissions.length;
  const pageCount = Math.max(1, Math.ceil(totalCommissions / rowsPerPage));
  const pageStart = (page - 1) * rowsPerPage;
  const pageEnd = Math.min(pageStart + rowsPerPage, totalCommissions);
  const visibleCommissions = sortedCommissions.slice(pageStart, pageEnd);

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
            Add Promotional
          </Typography>
          <form onSubmit={handleSubmit} style={{ width: '100%' }}>
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, mb: 2 }}>
              <Autocomplete
                options={doctors}
                getOptionLabel={option => option.name || ''}
                value={doctors.find(d => d.doctorId === form.doctorId) || null}
                onChange={(_, newValue) => {
                  setForm(f => ({ ...f, doctorId: newValue ? newValue.doctorId : undefined }));
                }}
                renderInput={params => (
                  <TextField {...params} label="Doctor" required size="small" sx={{ minWidth: 180, background: '#fff', borderRadius: 1 }} />
                )}
                isOptionEqualToValue={(option, value) => option.doctorId === value.doctorId}
                filterOptions={(options, { inputValue }) => {
                  const search = inputValue.trim().toLowerCase();
                  if (!search) return options;
                  return options.filter((d: any) =>
                    (d?.name || '').toLowerCase().includes(search)
                  );
                }}
                getOptionKey={option => option.doctorId}
                disableClearable
                autoHighlight
                filterSelectedOptions
                noOptionsText="No match"
              />
              <Autocomplete
                options={medicals}
                getOptionLabel={option => option.name || ''}
                value={medicals.find(m => m.medicalId === form.medicalId) || null}
                onChange={(_, newValue) => {
                  setForm(f => ({ ...f, medicalId: newValue ? newValue.medicalId : undefined }));
                }}
                renderInput={params => (
                  <TextField {...params} label="Medical" required size="small" sx={{ minWidth: 180, background: '#fff', borderRadius: 1 }} />
                )}
                renderOption={(props, option) => (
                  <li {...props} key={option.medicalId}>
                    {option.name}
                  </li>
                )}
                isOptionEqualToValue={(option, value) => option.medicalId === value.medicalId}
                filterOptions={(options, { inputValue }) =>
                  options.filter((m: any) => (m?.name || '').toLowerCase().includes(inputValue.trim().toLowerCase()))
                }
                disableClearable
                autoHighlight
                filterSelectedOptions
                noOptionsText="No match"
              />
              <Autocomplete
                options={products}
                getOptionLabel={option => option.name || ''}
                value={products.find(p => p.productId === form.productId) || null}
                onChange={(_, newValue) => {
                  setForm(f => ({ ...f, productId: newValue ? newValue.productId : undefined }));
                }}
                renderInput={params => (
                  <TextField {...params} label="Product" required size="small" sx={{ minWidth: 180, background: '#fff', borderRadius: 1 }} />
                )}
                renderOption={(props, option) => (
                  <li {...props} key={option.productId}>
                    {option.name}
                  </li>
                )}
                isOptionEqualToValue={(option, value) => option.productId === value.productId}
                filterOptions={(options, { inputValue }) =>
                  options.filter((p: any) => (p?.name || '').toLowerCase().includes(inputValue.trim().toLowerCase()))
                }
                disableClearable
                autoHighlight
                filterSelectedOptions
                noOptionsText="No match"
              />
              <TextField
                name="commissionPercentage"
                label="Promotional %"
                type="number"
                value={form.commissionPercentage}
                onChange={handleChange}
                inputProps={{ min: 0, step: 0.01, max: 100 }}
                required
                size="small"
                sx={{ minWidth: 120, background: '#fff', borderRadius: 1 }}
              />
            </Box>
            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-start', mt: 0.5 }}>
              <Button type="submit" variant="contained" color="primary" disabled={loading} sx={{ minWidth: 140, height: 40, whiteSpace: 'nowrap', fontWeight: 600, fontSize: 16, px: 2, boxShadow: 1 }}>
                {editingId ? 'Update' : 'Add'} Promotional
              </Button>
              {editingId && (
                <Button
                  type="button"
                  variant="outlined"
                  color="secondary"
                  onClick={() => { setEditingId(null); setForm({ commissionPercentage: 0 }); }}
                  sx={{ minWidth: 100, height: 40, whiteSpace: 'nowrap', fontWeight: 600, fontSize: 16, px: 2 }}
                >
                  Cancel
                </Button>
              )}
            </Box>
          </form>
        </CardContent>
      </Card>
      {/* Promotionals Section: Filters + Table merged */}
      <Card sx={{ width: '100%', maxWidth: 1100, m: '0 auto', boxShadow: 1, borderRadius: 2, flex: 1, display: 'flex', flexDirection: 'column' }}>
        <CardContent sx={{ p: 0 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', px: 3, py: 2, borderBottom: '1px solid #eee', background: '#f7f7f7', borderTopLeftRadius: 8, borderTopRightRadius: 8, gap: 2, flexWrap: 'wrap' }}>
            <Typography variant="h6" sx={{ flex: 1, fontWeight: 700, color: '#222' }}>
              Promotionals
            </Typography>
            <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#555', pr: 2 }}>
              Filters
            </Typography>
            <TextField
              type="text"
              placeholder="Search promotionals..."
              value={search}
              onChange={e => setSearch(e.target.value)}
              size="small"
              sx={{ width: { xs: '100%', sm: 320 }, background: '#fff', borderRadius: 1 }}
              InputProps={{ sx: { fontSize: 16 } }}
            />
            {/* Add more filter controls here if needed */}
          </Box>
          <Box sx={{ p: { xs: 1, sm: 3 }, pt: 2 }}>
            {loading && <LoadingSpinner />}
            <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
            <GlobalSnackbar open={showSuccessDialog} message={success || ''} severity="success" onClose={() => setShowSuccessDialog(false)} />
            <TableContainer component={Paper} sx={{ mt: 0, width: '100%', boxShadow: 0, borderRadius: 0 }}>
              <Table size="small" sx={{ minWidth: 650 }}>
                <TableHead sx={{ position: 'sticky', top: 0, background: '#f7f7f7', zIndex: 1 }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 700, width: '20%' }}>
                      <TableSortLabel
                        active={sortBy === 'doctor'}
                        direction={sortBy === 'doctor' ? sortOrder : 'asc'}
                        onClick={() => handleSort('doctor')}
                      >
                        Doctor
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '30%' }}>
                      <TableSortLabel
                        active={sortBy === 'medical'}
                        direction={sortBy === 'medical' ? sortOrder : 'asc'}
                        onClick={() => handleSort('medical')}
                      >
                        Medical
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '20%' }}>
                      <TableSortLabel
                        active={sortBy === 'product'}
                        direction={sortBy === 'product' ? sortOrder : 'asc'}
                        onClick={() => handleSort('product')}
                      >
                        Product
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '15%', whiteSpace: 'nowrap' }}>
                      <TableSortLabel
                        active={sortBy === 'commissionPercentage'}
                        direction={sortBy === 'commissionPercentage' ? sortOrder : 'asc'}
                        onClick={() => handleSort('commissionPercentage')}
                        sx={{ whiteSpace: 'nowrap' }}
                      >
                        Promotional %
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '15%' }}>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {visibleCommissions.length > 0 ? visibleCommissions.map((c, idx) => (
                      <TableRow key={c.commissionId} sx={{ background: idx % 2 === 0 ? '#fff' : '#f9fafb' }}>
                        {editingRowId === c.commissionId ? (
                          <>
                            <TableCell sx={{ width: '20%' }}>
                              <Autocomplete
                                options={doctors}
                                getOptionLabel={option => option.name || ''}
                                value={doctors.find(d => d.doctorId === rowEditForm?.doctorId) || null}
                                onChange={(_, newValue) => {
                                  setRowEditForm(f => f ? { ...f, doctorId: newValue ? newValue.doctorId : undefined } : f);
                                }}
                                renderInput={params => (
                                  <TextField {...params} label="Doctor" required size="small" sx={{ minWidth: 120, background: '#fff', borderRadius: 1 }} />
                                )}
                                renderOption={(props, option) => (
                                  <li {...props} key={option.doctorId}>
                                    {option.name}
                                  </li>
                                )}
                                isOptionEqualToValue={(option, value) => option.doctorId === value.doctorId}
                                filterOptions={(options, { inputValue }) =>
                                  options.filter((d: any) => (d?.name || '').toLowerCase().includes(inputValue.trim().toLowerCase()))
                                }
                                disableClearable
                                autoHighlight
                                filterSelectedOptions
                                noOptionsText="No match"
                              />
                            </TableCell>
                            <TableCell sx={{ width: '30%' }}>
                              <Autocomplete
                                options={medicals}
                                getOptionLabel={option => option.name || ''}
                                value={medicals.find(m => m.medicalId === rowEditForm?.medicalId) || null}
                                onChange={(_, newValue) => {
                                  setRowEditForm(f => f ? { ...f, medicalId: newValue ? newValue.medicalId : undefined } : f);
                                }}
                                renderInput={params => (
                                  <TextField {...params} label="Medical" required size="small" sx={{ minWidth: 120, background: '#fff', borderRadius: 1 }} />
                                )}
                                renderOption={(props, option) => (
                                  <li {...props} key={option.medicalId}>
                                    {option.name}
                                  </li>
                                )}
                                isOptionEqualToValue={(option, value) => option.medicalId === value.medicalId}
                                filterOptions={(options, { inputValue }) =>
                                  options.filter((m: any) => (m?.name || '').toLowerCase().includes(inputValue.trim().toLowerCase()))
                                }
                                disableClearable
                                autoHighlight
                                filterSelectedOptions
                                noOptionsText="No match"
                              />
                            </TableCell>
                            <TableCell sx={{ width: '20%' }}>
                              <Autocomplete
                                options={products}
                                getOptionLabel={option => option.name || ''}
                                value={products.find(p => p.productId === rowEditForm?.productId) || null}
                                onChange={(_, newValue) => {
                                  setRowEditForm(f => f ? { ...f, productId: newValue ? newValue.productId : undefined } : f);
                                }}
                                renderInput={params => (
                                  <TextField {...params} label="Product" required size="small" sx={{ minWidth: 120, background: '#fff', borderRadius: 1 }} />
                                )}
                                renderOption={(props, option) => (
                                  <li {...props} key={option.productId}>
                                    {option.name}
                                  </li>
                                )}
                                isOptionEqualToValue={(option, value) => option.productId === value.productId}
                                filterOptions={(options, { inputValue }) =>
                                  options.filter((p: any) => (p?.name || '').toLowerCase().includes(inputValue.trim().toLowerCase()))
                                }
                                disableClearable
                                autoHighlight
                                filterSelectedOptions
                                noOptionsText="No match"
                              />
                            </TableCell>
                            <TableCell sx={{ width: '15%' }}>
                              <TextField
                                name="commissionPercentage"
                                value={rowEditForm?.commissionPercentage ?? ''}
                                onChange={e => {
                                  const val = Number(e.target.value);
                                  if (val > 100) return;
                                  handleRowEditChange(e);
                                }}
                                size="small"
                                sx={{ width: '100%' }}
                                type="number"
                                inputProps={{ min: 0, max: 100, step: 0.01 }}
                              />
                            </TableCell>
                            <TableCell sx={{ width: '15%' }}>
                              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                <IconButton color="success" onClick={handleRowEditConfirm} size="small"><CheckIcon /></IconButton>
                                <IconButton color="inherit" onClick={handleRowEditCancel} size="small" sx={{ ml: 1 }}><CloseIcon /></IconButton>
                              </Box>
                            </TableCell>
                          </>
                        ) : (
                          <>
                            <TableCell sx={{ width: '20%' }}>
                              <Tooltip
                                title={(() => {
                                  const d = c.doctor || doctors.find(d => d.doctorId === (c.doctorId ?? (c.doctor as any)?.doctorId));
                                  if (!d) return '';
                                  return (
                                    <div>
                                      <div><b>Name:</b> {d.name || ''}</div>
                                      {d.specialization && <div><b>Specialization:</b> {d.specialization}</div>}
                                      {d.phoneNumber && <div><b>Phone:</b> {d.phoneNumber}</div>}
                                      {d.email && <div><b>Email:</b> {d.email}</div>}
                                    </div>
                                  );
                                })()}
                                arrow
                                placement="top"
                              >
                                <span style={{ cursor: 'pointer', textDecoration: 'underline dotted' }}>{c.doctor?.name || doctors.find(d => d.doctorId === (c.doctorId ?? (c.doctor as any)?.doctorId))?.name || ''}</span>
                              </Tooltip>
                            </TableCell>
                            <TableCell sx={{ width: '30%' }}>
                              <Tooltip
                                title={(() => {
                                  const m = c.medical || medicals.find(m => m.medicalId === (c.medicalId ?? (c.medical as any)?.medicalId));
                                  if (!m) return '';
                                  return (
                                    <div>
                                      <div><b>Name:</b> {m.name || ''}</div>
                                      {m.address && <div><b>Address:</b> {m.address}</div>}
                                      {m.phoneNumber && <div><b>Phone:</b> {m.phoneNumber}</div>}
                                      {m.email && <div><b>Email:</b> {m.email}</div>}
                                    </div>
                                  );
                                })()}
                                arrow
                                placement="top"
                              >
                                <span style={{ cursor: 'pointer', textDecoration: 'underline dotted' }}>{c.medical?.name || medicals.find(m => m.medicalId === (c.medicalId ?? (c.medical as any)?.medicalId))?.name || ''}</span>
                              </Tooltip>
                            </TableCell>
                            <TableCell sx={{ width: '20%' }}>
                              <Tooltip
                                title={(() => {
                                  const p = c.product || products.find(p => p.productId === (c.productId ?? (c.product as any)?.productId));
                                  if (!p) return '';
                                  return (
                                    <div>
                                      <div><b>Name:</b> {p.name || ''}</div>
                                      {p.description && <div><b>Description:</b> {p.description}</div>}
                                    </div>
                                  );
                                })()}
                                arrow
                                placement="top"
                              >
                                <span style={{ cursor: 'pointer', textDecoration: 'underline dotted' }}>{c.product?.name || products.find(p => p.productId === (c.productId ?? (c.product as any)?.productId))?.name || ''}</span>
                              </Tooltip>
                            </TableCell>
                            <TableCell sx={{ width: '15%' }}>
                              {c.commissionPercentage}
                            </TableCell>
                            <TableCell sx={{ width: '15%' }}>
                              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                <IconButton color="primary" onClick={() => handleRowEdit(c)} size="small"><EditIcon /></IconButton>
                                <IconButton color="error" onClick={() => handleDelete(c.commissionId!)} size="small" sx={{ ml: 1 }}><DeleteIcon /></IconButton>
                              </Box>
                            </TableCell>
                          </>
                        )}
                      </TableRow>
                  )) : (
                    <TableRow>
                      <TableCell colSpan={5} sx={{ textAlign: 'center', py: 4, color: '#666' }}>
                        No promotionals match your search.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
            <Box sx={{ mt: 2, display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: 'center', justifyContent: 'space-between', gap: 2, px: 1 }}>
              <Typography variant="body2" sx={{ color: '#555' }}>
                Showing {totalCommissions === 0 ? 0 : pageStart + 1} - {pageEnd} of {totalCommissions} promotionals
              </Typography>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems="center" sx={{ width: { xs: '100%', sm: 'auto' } }}>
                <FormControl size="small" sx={{ minWidth: 130, background: '#fff', borderRadius: 1 }}>
                  <InputLabel id="rows-per-page-label-commission">Page size</InputLabel>
                  <Select
                    labelId="rows-per-page-label-commission"
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
        message="Are you sure you want to delete this commission?"
        onClose={confirmed => {
          if (confirmed) {
            handleConfirmDelete();
          } else {
            setConfirmDeleteId(null);
          }
        }}
      />
    </Box>
  );
}
