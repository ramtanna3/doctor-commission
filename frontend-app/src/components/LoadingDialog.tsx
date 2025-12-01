import React from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';

export interface LoadingDialogProps {
  open: boolean;
  message?: string;
}

const LoadingDialog: React.FC<LoadingDialogProps> = ({ open, message }) => (
  <Dialog open={open} maxWidth="xs" fullWidth>
    <DialogTitle>Loading...</DialogTitle>
    <DialogContent sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 4 }}>
      <CircularProgress sx={{ mb: 2 }} />
      <DialogContentText>{message || 'Please wait.'}</DialogContentText>
    </DialogContent>
    <DialogActions />
  </Dialog>
);

export default LoadingDialog;
