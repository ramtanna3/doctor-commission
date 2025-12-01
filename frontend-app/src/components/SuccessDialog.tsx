import React from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';

export interface SuccessDialogProps {
  open: boolean;
  message: string;
  onClose: () => void;
}

const SuccessDialog: React.FC<SuccessDialogProps> = ({ open, message, onClose }) => (
  <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
    <DialogTitle>Success</DialogTitle>
    <DialogContent>
      <DialogContentText sx={{ textAlign: 'center', fontWeight: 600, fontSize: 18, py: 2 }}>
        {message}
      </DialogContentText>
    </DialogContent>
    <DialogActions sx={{ justifyContent: 'center' }}>
      <Button onClick={onClose} variant="contained" color="primary" autoFocus>OK</Button>
    </DialogActions>
  </Dialog>
);

export default SuccessDialog;
