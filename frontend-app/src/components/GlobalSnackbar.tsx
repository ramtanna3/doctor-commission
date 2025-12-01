import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import React from 'react';

export interface GlobalSnackbarProps {
  open: boolean;
  message: string;
  severity?: 'success' | 'error' | 'warning' | 'info';
  onClose: () => void;
  autoHideDuration?: number;
}

export default function GlobalSnackbar({ open, message, severity = 'info', onClose, autoHideDuration = 4000 }: GlobalSnackbarProps) {
  return (
    <Snackbar open={open} autoHideDuration={autoHideDuration} onClose={onClose} anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
      <Alert onClose={onClose} severity={severity} sx={{ width: '100%' }}>
        {message}
      </Alert>
    </Snackbar>
  );
}
