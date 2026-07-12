import './App.css';
import { useState } from 'react';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Drawer from '@mui/material/Drawer';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Divider from '@mui/material/Divider';
import Box from '@mui/material/Box';
import MedicalServicesIcon from '@mui/icons-material/MedicalServices';
import PersonIcon from '@mui/icons-material/Person';
import InventoryIcon from '@mui/icons-material/Inventory';
import MonetizationOnIcon from '@mui/icons-material/MonetizationOn';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import TableChartIcon from '@mui/icons-material/TableChart';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import SyncIcon from '@mui/icons-material/Sync';
import DashboardIcon from '@mui/icons-material/Dashboard';
import MedicalCrud from './MedicalCrud';
import DoctorCrud from './DoctorCrud';
import ProductCrud from './ProductCrud';
import CommissionCrud from './CommissionCrud';
import BulkMasterUpload from './BulkMasterUpload';
import ProcessSalesFile from './ProcessSalesFile';
import SalesTransactions from './SalesTransactions';
import DoctorBalances from './DoctorBalances';
import DoctorTransactions from './DoctorTransactions';
import AddDoctorTransaction from './AddDoctorTransaction';
import SyncDoctorWallet from './SyncDoctorWallet';

const SIDEBAR_MODULES = [
  {
    label: 'Master Data',
    items: [
      { key: 'doctor', label: 'Doctor', icon: <PersonIcon /> },
      { key: 'medical', label: 'Medical', icon: <MedicalServicesIcon /> },
      { key: 'product', label: 'Product', icon: <InventoryIcon /> },
  { key: 'commission', label: 'Promotional Setup', icon: <MonetizationOnIcon /> },
      { key: 'bulk-upload', label: 'Bulk Master Upload', icon: <UploadFileIcon /> },
    ],
  },
  {
    label: 'Sales',
    items: [
      { key: 'process-sales', label: 'Process Sales File', icon: <ReceiptLongIcon /> },
      { key: 'sales-transactions', label: 'Sales Transactions', icon: <TableChartIcon /> },
    ],
  },
  {
    label: 'Doctor Ledger',
    items: [
      { key: 'doctor-balances', label: 'Doctor Balances', icon: <AccountBalanceWalletIcon /> },
      { key: 'doctor-transactions', label: 'Doctor Transactions', icon: <TableChartIcon /> },
      { key: 'add-doctor-transaction', label: 'Add Doctor Transaction', icon: <AddCircleOutlineIcon /> },
      { key: 'sync-doctor-wallet', label: 'Sync Doctor Wallet', icon: <SyncIcon /> },
    ],
  },
];

function App() {
  const [selected, setSelected] = useState('doctor');

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', background: '#f5f5f5', m: 0, p: 0, width: '100vw', boxSizing: 'border-box', overflowX: 'hidden' }}>
      {/* Header */}
      <AppBar position="fixed" sx={{ zIndex: 1201 }} color="primary">
        <Toolbar>
          <DashboardIcon sx={{ mr: 2 }} />
          <Typography variant="h6" noWrap component="div">
            DCAS Admin
          </Typography>
        </Toolbar>
      </AppBar>
      <Box sx={{ display: 'flex', flex: 1, pt: 8 }}>
        {/* Left Sidebar */}
        <Drawer
          variant="permanent"
          sx={{
            width: 260,
            flexShrink: 0,
            zIndex: 1200,
            [`& .MuiDrawer-paper`]: {
              width: 260,
              boxSizing: 'border-box',
              background: '#f5f5f5',
              borderRight: '1px solid #e0e0e0',
              boxShadow: 'none',
              margin: 0,
              borderLeft: 'none',
            },
          }}
        >
          <Toolbar />
          <Box sx={{ overflow: 'auto', mt: 2 }}>
            {SIDEBAR_MODULES.map((module, idx) => (
              <Box key={module.label} sx={{ mb: 2 }}>
                <Typography variant="subtitle2" sx={{ pl: 2, pb: 1, color: '#666', fontWeight: 700, textTransform: 'uppercase', letterSpacing: 1 }}>
                  {module.label}
                </Typography>
                <List>
                  {module.items.map(item => (
                    <ListItem key={item.key} disablePadding>
                      <ListItemButton
                        selected={selected === item.key}
                        onClick={() => setSelected(item.key)}
                      >
                        <ListItemIcon sx={{ minWidth: 36 }}>{item.icon}</ListItemIcon>
                        <ListItemText primary={item.label} primaryTypographyProps={{ fontWeight: selected === item.key ? 700 : 400 }} />
                      </ListItemButton>
                    </ListItem>
                  ))}
                </List>
                {idx < SIDEBAR_MODULES.length - 1 && <Divider sx={{ mt: 1 }} />}
              </Box>
            ))}
          </Box>
        </Drawer>
        {/* Right Main Content */}
        <Box component="main" sx={{
          flex: 1,
          p: 0,
          minHeight: 'calc(100vh - 64px)',
          background: '#f5f5f5',
          display: 'flex',
          flexDirection: 'column',
          margin: 0,
          border: 'none',
        }}>
          <Box sx={{ flex: 1, width: '100%', maxWidth: 1200, mx: 'auto', p: { xs: 1, sm: 2 }, boxSizing: 'border-box', display: 'flex', flexDirection: 'column', background: 'transparent' }}>
            {selected === 'doctor' && <DoctorCrud />}
            {selected === 'medical' && <MedicalCrud />}
            {selected === 'product' && <ProductCrud />}
            {selected === 'commission' && <CommissionCrud />}
            {selected === 'bulk-upload' && <BulkMasterUpload />}
            {selected === 'process-sales' && <ProcessSalesFile />}
            {selected === 'sales-transactions' && <SalesTransactions />}
            {selected === 'doctor-balances' && <DoctorBalances />}
            {selected === 'doctor-transactions' && <DoctorTransactions />}
            {selected === 'add-doctor-transaction' && <AddDoctorTransaction />}
            {selected === 'sync-doctor-wallet' && <SyncDoctorWallet />}
          </Box>
        </Box>
      </Box>
    </Box>
  );
}

export default App;
