import React from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';

export interface ConfirmDialogProps {
  open: boolean;
  title?: string;
  message: string;
  onClose: (confirmed: boolean) => void;
  confirmText?: string;
  cancelText?: string;
}

const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
  open,
  title = 'Confirm',
  message,
  onClose,
  confirmText = 'Yes',
  cancelText = 'No',
}) => (
  <Dialog open={open} onClose={() => onClose(false)}>
    <DialogTitle>{title}</DialogTitle>
    <DialogContent>
      <DialogContentText>{message}</DialogContentText>
    </DialogContent>
    <DialogActions>
      <Button onClick={() => onClose(false)} color="secondary">{cancelText}</Button>
      <Button onClick={() => onClose(true)} color="primary" autoFocus>{confirmText}</Button>
    </DialogActions>
  </Dialog>
);

export default ConfirmDialog;
