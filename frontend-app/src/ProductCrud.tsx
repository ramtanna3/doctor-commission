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

export type Product = {
  productId?: number;
  name: string;
  description: string;
  defaultCommissionPercentage?: number;
  isActive?: boolean;
};

const API_BASE = '/api/products';

export default function ProductCrud() {
  const [products, setProducts] = useState<Product[]>([]);
  const [form, setForm] = useState<Product>({ name: '', description: '', defaultCommissionPercentage: undefined });
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingRowId, setEditingRowId] = useState<number | null>(null);
  const [rowEditForm, setRowEditForm] = useState<Product | null>(null);
  const [formErrors, setFormErrors] = useState<{ defaultCommissionPercentage?: string }>({});
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showSuccessDialog, setShowSuccessDialog] = useState(false);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState<'name' | 'description'>('name');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [confirmDeleteId, setConfirmDeleteId] = useState<number | null>(null);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const res = await fetch(API_BASE, {
        headers: { 'x-user-id': '1' }
      });
      if (!res.ok) throw new Error('Failed to fetch products');
      setProducts(await res.json());
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchProducts(); }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const method = editingId ? 'PUT' : 'POST';
      const url = editingId ? `${API_BASE}/${editingId}` : API_BASE;
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json', 'x-user-id': '1' },
        body: JSON.stringify(form),
      });
      if (!res.ok) throw new Error('Failed to save product');
  setForm({ name: '', description: '' });
  setEditingId(null);
  setSuccess(editingId ? 'Product updated successfully!' : 'Product added successfully!');
  fetchProducts();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleRowEdit = (product: Product) => {
    setEditingRowId(product.productId!);
    setRowEditForm({ ...product });
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
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE}/${editingRowId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'x-user-id': '1' },
        body: JSON.stringify(rowEditForm),
      });
      if (!res.ok) throw new Error('Failed to update product');
      setSuccess('Product updated successfully!');
      setShowSuccessDialog(true);
      setEditingRowId(null);
      setRowEditForm(null);
      fetchProducts();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
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
      setSuccess('Product deleted successfully!');
      setShowSuccessDialog(true);
      fetchProducts();
    } catch (e: any) {
      setError(e.message);
    } finally {
      setLoading(false);
      setConfirmDeleteId(null);
    }
  };

  const handleSort = (column: 'name' | 'description') => {
    if (sortBy === column) {
      setSortOrder(prev => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(column);
      setSortOrder('asc');
    }
  };

  return (
    <Box sx={{ flex: 1, width: '100%', minHeight: 'calc(100vh - 64px)', background: '#f5f5f5', p: { xs: 1, sm: 1 }, boxSizing: 'border-box', display: 'flex', flexDirection: 'column' }}>
      {/* Add Product Section */}
      <Card sx={{ width: '100%', maxWidth: 1100, m: '0 auto', mb: 3, boxShadow: 2, borderRadius: 3, background: '#f9fafb' }}>
        <CardContent sx={{ p: { xs: 2, sm: 3 } }}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1976d2', mb: 2, pl: 1 }}>
            Add Product
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
                  sx={{ flex: 1, minWidth: 140, background: '#fff', borderRadius: 1 }}
                  FormHelperTextProps={{ sx: { minHeight: 24, background: '#fff', m: 0, p: 0 } }}
                />
                <TextField
                  name="description"
                  label="Description"
                  value={form.description}
                  onChange={handleChange}
                  size="small"
                  sx={{ flex: 1, minWidth: 120, background: '#fff', borderRadius: 1 }}
                  FormHelperTextProps={{ sx: { minHeight: 24, background: '#fff', m: 0, p: 0 } }}
                />
                <TextField
                  name="defaultCommissionPercentage"
                  label="Default Commission %"
                  value={form.defaultCommissionPercentage ?? ''}
                  onChange={e => {
                    const val = e.target.value;
                    if (val === '' || (/^\d{0,3}(\.\d{0,2})?$/.test(val) && Number(val) <= 100)) {
                      setForm(f => ({ ...f, defaultCommissionPercentage: val === '' ? undefined : Number(val) }));
                      setFormErrors(errs => ({ ...errs, defaultCommissionPercentage: undefined }));
                    } else {
                      setFormErrors(errs => ({ ...errs, defaultCommissionPercentage: '0-100, up to 2 decimals' }));
                    }
                  }}
                  size="small"
                  sx={{ flex: 1, minWidth: 120, background: '#fff', borderRadius: 1 }}
                  type="number"
                  inputProps={{ min: 0, max: 100, step: 0.01 }}
                  error={!!formErrors.defaultCommissionPercentage}
                  helperText={formErrors.defaultCommissionPercentage || ''}
                  FormHelperTextProps={{ sx: { minHeight: 24, background: '#fff', m: 0, p: 0 } }}
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
                  {editingId ? 'Update' : 'Add'} Product
                </Button>
                {editingId && (
                  <Button
                    type="button"
                    variant="outlined"
                    color="secondary"
                    onClick={() => { setEditingId(null); setForm({ name: '', description: '', defaultCommissionPercentage: undefined }); }}
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
      {/* Products Section: Filters + Table merged */}
      <Card sx={{ width: '100%', maxWidth: 1100, m: '0 auto', boxShadow: 1, borderRadius: 2, flex: 1, display: 'flex', flexDirection: 'column' }}>
        <CardContent sx={{ p: 0 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', px: 3, py: 2, borderBottom: '1px solid #eee', background: '#f7f7f7', borderTopLeftRadius: 8, borderTopRightRadius: 8, gap: 2, flexWrap: 'wrap' }}>
            <Typography variant="h6" sx={{ flex: 1, fontWeight: 700, color: '#222' }}>
              Products
            </Typography>
            <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#555', pr: 2 }}>
              Filters
            </Typography>
            <TextField
              type="text"
              placeholder="Search products..."
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
                    <TableCell sx={{ fontWeight: 700, width: '40%' }}>
                      <TableSortLabel
                        active={sortBy === 'name'}
                        direction={sortBy === 'name' ? sortOrder : 'asc'}
                        onClick={() => handleSort('name')}
                      >
                        Name
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '20%' }}>
                      <TableSortLabel
                        active={sortBy === 'description'}
                        direction={sortBy === 'description' ? sortOrder : 'asc'}
                        onClick={() => handleSort('description')}
                      >
                        Description
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '20%' }}>Default Commission %</TableCell>
                    <TableCell sx={{ fontWeight: 700, width: '20%' }}>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {products
                    .filter(p => {
                      const s = search.toLowerCase();
                      return (
                        (p.name || '').toLowerCase().includes(s) ||
                        (p.description || '').toLowerCase().includes(s) ||
                        (p.defaultCommissionPercentage !== undefined && String(p.defaultCommissionPercentage).includes(s))
                      );
                    })
                    .sort((a, b) => {
                      const aVal = (a[sortBy] || '').toLowerCase();
                      const bVal = (b[sortBy] || '').toLowerCase();
                      if (aVal < bVal) return sortOrder === 'asc' ? -1 : 1;
                      if (aVal > bVal) return sortOrder === 'asc' ? 1 : -1;
                      return 0;
                    })
                  .map((p, idx) => (
                    <TableRow key={p.productId} sx={{ background: idx % 2 === 0 ? '#fff' : '#f9fafb' }}>
                      {editingRowId === p.productId ? (
                        <>
                          <TableCell sx={{ width: '40%' }}>
                            <TextField name="name" value={rowEditForm?.name || ''} onChange={handleRowEditChange} size="small" sx={{ width: '100%' }} />
                          </TableCell>
                          <TableCell sx={{ width: '20%' }}>
                            <TextField name="description" value={rowEditForm?.description || ''} onChange={handleRowEditChange} size="small" sx={{ width: '100%' }} />
                          </TableCell>
                          <TableCell sx={{ width: '20%' }}>
                            <TextField
                              name="defaultCommissionPercentage"
                              value={rowEditForm?.defaultCommissionPercentage ?? ''}
                              onChange={e => {
                                const val = e.target.value;
                                if (val === '' || (/^\d{0,3}(\.\d{0,2})?$/.test(val) && Number(val) <= 100)) {
                                  setRowEditForm(f => f ? { ...f, defaultCommissionPercentage: val === '' ? undefined : Number(val) } : f);
                                }
                              }}
                              size="small"
                              sx={{ width: '100%' }}
                              type="number"
                              inputProps={{ min: 0, max: 100, step: 0.01 }}
                            />
                          </TableCell>
                          <TableCell sx={{ width: '20%' }}>
                            <IconButton color="success" onClick={handleRowEditConfirm} size="small"><CheckIcon /></IconButton>
                            <IconButton color="inherit" onClick={handleRowEditCancel} size="small"><CloseIcon /></IconButton>
                          </TableCell>
                        </>
                      ) : (
                        <>
                          <TableCell sx={{ width: '40%' }}>{p.name}</TableCell>
                          <TableCell sx={{ width: '20%' }}>{p.description}</TableCell>
                          <TableCell sx={{ width: '20%' }}>{p.defaultCommissionPercentage ?? ''}</TableCell>
                          <TableCell sx={{ width: '20%' }}>
                            <IconButton color="primary" onClick={() => handleRowEdit(p)} size="small"><EditIcon /></IconButton>
                            <IconButton color="error" onClick={() => setConfirmDeleteId(p.productId!)} size="small" sx={{ ml: 1 }}><DeleteIcon /></IconButton>
                          </TableCell>
                        </>
                      )}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Box>
        </CardContent>
      </Card>
      <ConfirmDialog
        open={!!confirmDeleteId}
        title="Confirm Delete"
        message="Are you sure you want to delete this product?"
        onClose={confirmed => {
          if (confirmed) handleConfirmDelete();
          else setConfirmDeleteId(null);
        }}
      />
    </Box>
  );
}
