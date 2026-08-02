import { useState } from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import Divider from '@mui/material/Divider';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import Alert from '@mui/material/Alert';
import { apiFetch, updateProfileCache } from './api';

interface Props {
  open: boolean;
  onClose: () => void;
  displayName: string;
  companyName: string;
  onProfileUpdated: (displayName: string, companyName: string) => void;
}

export default function UserProfileDialog({ open, onClose, displayName, companyName, onProfileUpdated }: Props) {
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [profile, setProfile] = useState<any>(null);

  const handleEnter = async () => {
    setError(null);
    setSuccess(false);
    setProfile(null);
    setLoading(true);
    try {
      const res = await apiFetch('/api/auth/profile');
      if (res.ok) {
        setProfile(await res.json());
      } else {
        setError('Failed to load profile.');
      }
    } catch {
      setError('Unable to connect to server.');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setError(null);
    setSuccess(false);
    onClose();
  };

  const handleChange = (field: string, value: string) => {
    setProfile((p: any) => ({ ...p, [field]: value }));
  };

  const handleSave = async () => {
    setSaving(true);
    setError(null);
    setSuccess(false);
    try {
      const res = await apiFetch('/api/auth/profile', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          firstName: profile.firstName || '',
          lastName: profile.lastName || '',
          email: profile.email || '',
          phoneNumber: profile.phoneNumber || '',
          companyName: profile.companyName || '',
          companyAddress: profile.companyAddress || '',
          companyEmail: profile.companyEmail || '',
          companyPhone: profile.companyPhone || '',
        }),
      });
      if (res.ok) {
        setSuccess(true);
        const newDisplayName = [profile.firstName, profile.lastName].filter(Boolean).join(' ') || displayName;
        const newCompanyName = profile.companyName || companyName;
        updateProfileCache(newDisplayName, newCompanyName);
        onProfileUpdated(newDisplayName, newCompanyName);
      } else {
        const data = await res.json().catch(() => ({}));
        setError(data.error || 'Failed to save profile.');
      }
    } catch {
      setError('Unable to connect to server.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="sm"
      fullWidth
      TransitionProps={{ onEnter: handleEnter }}
      PaperProps={{ sx: { borderRadius: 3 } }}
    >
      <DialogTitle sx={{ fontWeight: 700, color: '#1976d2' }}>My Profile</DialogTitle>
      <DialogContent>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 5 }}>
            <CircularProgress />
          </Box>
        ) : profile ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 0.5 }}>
            {error && <Alert severity="error" onClose={() => setError(null)}>{error}</Alert>}
            {success && <Alert severity="success">Profile updated successfully!</Alert>}

            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#888', textTransform: 'uppercase', letterSpacing: 0.8, fontSize: 11 }}>
              Personal Details
            </Typography>
            <TextField
              label="Username"
              value={profile.username || ''}
              disabled
              size="small"
              helperText="Username cannot be changed"
            />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField
                label="First Name"
                value={profile.firstName || ''}
                onChange={e => handleChange('firstName', e.target.value)}
                size="small"
                fullWidth
              />
              <TextField
                label="Last Name"
                value={profile.lastName || ''}
                onChange={e => handleChange('lastName', e.target.value)}
                size="small"
                fullWidth
              />
            </Box>
            <TextField
              label="Email"
              value={profile.email || ''}
              onChange={e => handleChange('email', e.target.value)}
              size="small"
              fullWidth
            />
            <TextField
              label="Phone Number"
              value={profile.phoneNumber || ''}
              onChange={e => handleChange('phoneNumber', e.target.value)}
              size="small"
              fullWidth
            />

            <Divider sx={{ my: 0.5 }} />

            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#888', textTransform: 'uppercase', letterSpacing: 0.8, fontSize: 11 }}>
              Company Details
            </Typography>
            <TextField
              label="Company Name"
              value={profile.companyName || ''}
              onChange={e => handleChange('companyName', e.target.value)}
              size="small"
              fullWidth
            />
            <TextField
              label="Address"
              value={profile.companyAddress || ''}
              onChange={e => handleChange('companyAddress', e.target.value)}
              size="small"
              fullWidth
            />
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField
                label="Company Email"
                value={profile.companyEmail || ''}
                onChange={e => handleChange('companyEmail', e.target.value)}
                size="small"
                fullWidth
              />
              <TextField
                label="Company Phone"
                value={profile.companyPhone || ''}
                onChange={e => handleChange('companyPhone', e.target.value)}
                size="small"
                fullWidth
              />
            </Box>
          </Box>
        ) : error ? (
          <Alert severity="error">{error}</Alert>
        ) : null}
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={handleClose} color="inherit">Cancel</Button>
        <Button
          onClick={handleSave}
          variant="contained"
          disabled={saving || loading || !profile}
          startIcon={saving ? <CircularProgress size={16} color="inherit" /> : undefined}
        >
          {saving ? 'Saving...' : 'Save Changes'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
