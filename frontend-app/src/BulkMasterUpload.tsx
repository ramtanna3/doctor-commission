import { useRef, useState } from 'react';
import GlobalSnackbar from './components/GlobalSnackbar';
import SuccessDialog from './components/SuccessDialog';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import IconButton from '@mui/material/IconButton';
import DeleteIcon from '@mui/icons-material/Delete';
import LinearProgress from '@mui/material/LinearProgress';

function BulkMasterUpload() {
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showSuccessDialog, setShowSuccessDialog] = useState(false);
  const [loading, setLoading] = useState(false);
  const [progress, setProgress] = useState<number>(0);
  const inputRef = useRef<HTMLInputElement>(null);

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
    if (!file) {
      setError('Please select a file.');
      return;
    }
    setLoading(true);
    setProgress(30);
    setError(null);
    setSuccess(null);
    const formData = new FormData();
    formData.append('file', file);
    try {
      const res = await fetch('/api/master-data/upload', {
        method: 'POST',
  headers: { 'x-user-id': '1' },
        body: formData,
      });
      setProgress(80);
      if (!res.ok) throw new Error('Upload failed');
      setProgress(100);
      setSuccess('Upload successful!');
      setShowSuccessDialog(true);
      setFile(null);
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
      <Card sx={{ width: '100%', maxWidth: 500, m: '0 auto', mt: 5, boxShadow: 2, borderRadius: 3, background: '#f9fafb' }}>
        <CardContent sx={{ p: { xs: 2, sm: 3 } }}>
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#1976d2', mb: 2, pl: 1 }}>
            Bulk Master Upload
          </Typography>
          <form onSubmit={handleUpload} style={{ width: '100%' }}>
            <Box
              onDrop={handleDrop}
              onDragOver={handleDragOver}
              sx={{
                border: '2px dashed #90caf9',
                borderRadius: 2,
                p: 3,
                mb: 2,
                textAlign: 'center',
                background: file ? '#f5fafd' : '#fafbfc',
                cursor: 'pointer',
                transition: 'background 0.2s',
                '&:hover': { background: '#e3f2fd' },
              }}
              onClick={() => inputRef.current?.click()}
            >
              <CloudUploadIcon sx={{ fontSize: 48, color: '#90caf9', mb: 1 }} />
              <Typography variant="body1" sx={{ color: '#555', mb: 1 }}>
                Drag & drop a file here, or click to select
              </Typography>
              <input
                ref={inputRef}
                type="file"
                accept=".xlsx,.xls,.csv"
                style={{ display: 'none' }}
                onChange={handleFileChange}
              />
              {file && (
                <Box sx={{ mt: 2, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1 }}>
                  <Typography variant="body2" sx={{ fontWeight: 600, color: '#1976d2' }}>{file.name}</Typography>
                  <IconButton size="small" onClick={e => { e.stopPropagation(); handleRemoveFile(); }}>
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </Box>
              )}
            </Box>
            {progress > 0 && (
              <Box sx={{ width: '100%', mb: 2 }}>
                <LinearProgress variant="determinate" value={progress} />
              </Box>
            )}
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={loading || !file}
              sx={{ width: '100%', height: 44, fontWeight: 700, fontSize: 16, mt: 1 }}
            >
              {loading ? 'Uploading...' : 'Upload'}
            </Button>
          </form>
          <GlobalSnackbar open={!!error} message={error || ''} severity="error" onClose={() => setError(null)} />
          <SuccessDialog open={showSuccessDialog} message={success || ''} onClose={() => setShowSuccessDialog(false)} />
        </CardContent>
      </Card>
    </Box>
  );
}

export default BulkMasterUpload;
