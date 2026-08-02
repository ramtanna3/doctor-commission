import { useRef, useState, useEffect } from 'react';
import { apiFetch } from './api';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import IconButton from '@mui/material/IconButton';
import DeleteIcon from '@mui/icons-material/Delete';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import LinearProgress from '@mui/material/LinearProgress';
// import TableContainer from '@mui/material/TableContainer';
import CheckIcon from '@mui/icons-material/Check';
import WarningIcon from '@mui/icons-material/Warning';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import GlobalSnackbar from './components/GlobalSnackbar';
import SuccessDialog from './components/SuccessDialog';
import LoadingSpinner from './components/LoadingSpinner';

export default function ProcessSalesFile({ onSuccess }: { onSuccess?: () => void }) {
  const [file, setFile] = useState<File | null>(null);
  const [distributors, setDistributors] = useState<any[]>([]);
  const [selectedDistributor, setSelectedDistributor] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showSuccessDialog, setShowSuccessDialog] = useState(false);
  const [loading, setLoading] = useState(false);
  const [progress, setProgress] = useState<number>(0);
  const [apiResponse, setApiResponse] = useState<any>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    fetchDistributors();
  }, []);

  const fetchDistributors = async () => {
  const res = await apiFetch('/api/distributors');
    if (res.ok) setDistributors(await res.json());
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files?.[0]) {
      setFile(e.target.files[0]);
      setError(null);
      setSuccess(null);
    }
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      setFile(e.dataTransfer.files[0]);
      setError(null);
      setSuccess(null);
    }
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
  };

  const handleRemoveFile = () => {
    setFile(null);
    setError(null);
    setSuccess(null);
    if (inputRef.current) inputRef.current.value = '';
  };


  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file || !selectedDistributor) {
      setError('Please select a file and distributor.');
      return;
    }
    setLoading(true);
    setProgress(30);
    setError(null);
    setSuccess(null);
    setApiResponse(null);
    const formData = new FormData();
    formData.append('file', file);
    formData.append('distributorId', selectedDistributor);
    try {
      const res = await apiFetch('/api/commissions/process-sales-excel', {
        method: 'POST',
        body: formData,
      });
      setProgress(80);
      if (!res.ok) throw new Error('Upload failed');
      const data = await res.json();
      setApiResponse(data);
      setProgress(100);
      // Only show success if at least one count is not null and no errors
      if ([data.totalRows, data.successCount, data.nigoCount, data.unmatchedCount, data.errorCount].some(v => v != null) && !(data.errors && data.errors.length > 0)) {
        setSuccess('Sales file processed successfully!');
        setShowSuccessDialog(true);
        if (onSuccess) onSuccess();
      }
      setFile(null);
      setSelectedDistributor('');
      if (inputRef.current) inputRef.current.value = '';
    } catch (e: any) {
      setError(e.message || 'Upload failed');
      setProgress(0);
    } finally {
      setLoading(false);
      setTimeout(() => setProgress(0), 1000);
    }
  };

  return (
    <Box sx={{ flex: 1, width: '100%', minHeight: 'calc(100vh - 64px)', background: '#f5f5f5', p: { xs: 1, sm: 1 }, boxSizing: 'border-box', display: 'flex', flexDirection: 'column' }}>
  <Card sx={{ width: '100%', maxWidth: 1100, m: '0 auto', mt: 3, boxShadow: 2, borderRadius: 3, background: '#f9fafb' }}>
        <CardContent sx={{ p: { xs: 2, sm: 3 } }}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1976d2', mb: 2, pl: 1 }}>
            Process Sales File
          </Typography>
          <form onSubmit={handleUpload} style={{ width: '100%' }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              {/* Row 1: Distributor and File Upload */}
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <Autocomplete
                getOptionKey={option => option.id || option.distributorId}
                  options={distributors}
                  getOptionLabel={option => option.name || option.distributorName || `Distributor #${option.id || option.distributorId}`}
                  value={distributors.find(d => (d.id || d.distributorId) == selectedDistributor) || null}
                  onChange={(_, newValue) => setSelectedDistributor(newValue ? (newValue.id || newValue.distributorId).toString() : '')}
                  renderInput={params => (
                    <TextField {...params} label="Distributor" required size="small" sx={{ flex: 1, background: '#fff', borderRadius: 1 }} />
                  )}
                  isOptionEqualToValue={(option, value) => (option.id || option.distributorId) === (value.id || value.distributorId)}
                  disableClearable
                  autoHighlight
                  filterSelectedOptions
                  noOptionsText="No match"
                  sx={{ flex: 1 }}
                />
                <Box
                  onDrop={handleDrop}
                  onDragOver={handleDragOver}
                  sx={{ flex: 1, display: 'flex', alignItems: 'center' }}
                >
                  <TextField
                    label="Sales File"
                    value={file ? file.name : ''}
                    placeholder="Drag & drop or click to select"
                    size="small"
                    fullWidth
                    InputProps={{
                      readOnly: true,
                      endAdornment: file ? (
                        <IconButton size="small" onClick={handleRemoveFile}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      ) : null,
                      startAdornment: (
                        <IconButton size="small" onClick={() => inputRef.current?.click()}>
                          <CloudUploadIcon fontSize="small" />
                        </IconButton>
                      ),
                    }}
                    sx={{ flex: 1, background: '#fff', borderRadius: 1, cursor: 'pointer' }}
                    onClick={() => inputRef.current?.click()}
                    onDrop={handleDrop}
                    onDragOver={handleDragOver}
                  />
                  <input
                    ref={inputRef}
                    type="file"
                    accept=".xlsx,.xls,.csv"
                    style={{ display: 'none' }}
                    onChange={handleFileChange}
                  />
                </Box>
              </Box>
              {/* Row 2: Button */}
              <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-start', mt: 0.5 }}>
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  disabled={loading || !file || !selectedDistributor}
                  sx={{ minWidth: 140, height: 40, whiteSpace: 'nowrap', fontWeight: 600, fontSize: 16, px: 2, boxShadow: 1 }}
                >
                  {loading ? 'Processing...' : 'Process'}
                </Button>
              </Box>
              {progress > 0 && (
                <Box sx={{ width: '100%', mb: 2 }}>
                  <LinearProgress variant="determinate" value={progress} />
                </Box>
              )}
            </Box>
          </form>
          {loading && <LoadingSpinner />}
          <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
          <SuccessDialog open={showSuccessDialog} message={success || ''} onClose={() => setShowSuccessDialog(false)} />
          {apiResponse && (
            <Card sx={{ mt: 4, background: '#f8f8f8', borderRadius: 2, boxShadow: 0 }}>
              <CardContent>
                <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 2, color: '#1976d2', display: 'flex', alignItems: 'center', gap: 1 }}>
                  <CloudUploadIcon sx={{ color: '#1976d2', mr: 1 }} /> Sales File Processing Result
                </Typography>
                {(apiResponse.errors && apiResponse.errors.length > 0) ||
                  ([apiResponse.totalRows, apiResponse.successCount, apiResponse.nigoCount, apiResponse.unmatchedCount, apiResponse.errorCount].every(v => v == null)) ? (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, background: '#ffebee', borderRadius: 2, p: 3, mt: 2 }}>
                    <ErrorOutlineIcon sx={{ color: '#d32f2f', fontSize: 40 }} />
                    <Box>
                      <Typography variant="h6" sx={{ color: '#d32f2f', fontWeight: 700, mb: 1 }}>File Processing Failed</Typography>
                      {apiResponse.errors && apiResponse.errors.length > 0 ? (
                        apiResponse.errors.map((err: string, idx: number) => {
                          // Remove technical details from error message
                          const cleaned = err.replace(/\(attempted merging values [^)]+\)/g, '').replace(/\s+/g, ' ').trim();
                          return <Typography key={idx} variant="body2" sx={{ color: '#d32f2f' }}>{cleaned}</Typography>;
                        })
                      ) : (
                        <Typography variant="body2" sx={{ color: '#d32f2f' }}>Unknown error occurred.</Typography>
                      )}
                    </Box>
                  </Box>
                ) : (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, justifyContent: 'flex-start', alignItems: 'stretch', mt: 1 }}>
                    <Box sx={{ flex: 1, minWidth: 180, background: '#fff', borderRadius: 2, p: 2, display: 'flex', alignItems: 'center', gap: 2, boxShadow: 1 }}>
                      <Typography sx={{ color: '#1976d2', fontWeight: 700 }}><CloudUploadIcon /></Typography>
                      <Box>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>Total Rows</Typography>
                        <Typography variant="h6" sx={{ fontWeight: 700 }}>{apiResponse.totalRows ?? 0}</Typography>
                      </Box>
                    </Box>
                    <Box sx={{ flex: 1, minWidth: 180, background: '#e8f5e9', borderRadius: 2, p: 2, display: 'flex', alignItems: 'center', gap: 2, boxShadow: 1 }}>
                      <Typography sx={{ color: '#388e3c', fontWeight: 700 }}><CheckIcon /></Typography>
                      <Box>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>Success Count</Typography>
                        <Typography variant="h6" sx={{ fontWeight: 700 }}>{apiResponse.successCount ?? 0}</Typography>
                      </Box>
                    </Box>
                    <Box sx={{ flex: 1, minWidth: 180, background: '#fffde7', borderRadius: 2, p: 2, display: 'flex', alignItems: 'center', gap: 2, boxShadow: 1 }}>
                      <Typography sx={{ color: '#fbc02d', fontWeight: 700 }}><WarningIcon /></Typography>
                      <Box>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>NIGO Count</Typography>
                        <Typography variant="h6" sx={{ fontWeight: 700 }}>{apiResponse.nigoCount ?? 0}</Typography>
                      </Box>
                    </Box>
                    <Box sx={{ flex: 1, minWidth: 180, background: '#e3f2fd', borderRadius: 2, p: 2, display: 'flex', alignItems: 'center', gap: 2, boxShadow: 1 }}>
                      <Typography sx={{ color: '#0288d1', fontWeight: 700 }}><HelpOutlineIcon /></Typography>
                      <Box>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>Unmatched Count</Typography>
                        <Typography variant="h6" sx={{ fontWeight: 700 }}>{apiResponse.unmatchedCount ?? 0}</Typography>
                      </Box>
                    </Box>
                    <Box sx={{ flex: 1, minWidth: 180, background: '#ffebee', borderRadius: 2, p: 2, display: 'flex', alignItems: 'center', gap: 2, boxShadow: 1 }}>
                      <Typography sx={{ color: '#d32f2f', fontWeight: 700 }}><ErrorOutlineIcon /></Typography>
                      <Box>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>Error Count</Typography>
                        <Typography variant="h6" sx={{ fontWeight: 700 }}>{apiResponse.errorCount ?? 0}</Typography>
                      </Box>
                    </Box>
                  </Box>
                )}
              </CardContent>
            </Card>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
