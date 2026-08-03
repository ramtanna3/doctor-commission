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
import Button from '@mui/material/Button';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Tooltip from '@mui/material/Tooltip';
import LogoutIcon from '@mui/icons-material/Logout';
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
import LockIcon from '@mui/icons-material/Lock';
import DashboardIcon from '@mui/icons-material/Dashboard';
import RefreshIcon from '@mui/icons-material/Refresh';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
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
import ChangePassword from './ChangePassword';
import Login from './Login';
import UserProfileDialog from './UserProfileDialog';
import { isLoggedIn, clearToken, getDisplayName, getUsername, getCompanyName } from './api';

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
  {
    label: 'Account',
    items: [
      { key: 'change-password', label: 'Change Password', icon: <LockIcon /> },
    ],
  },
];

function App() {
  const [loggedIn, setLoggedIn] = useState(isLoggedIn());
  const [selected, setSelected] = useState('doctor');
  const [refreshKey, setRefreshKey] = useState(0);
  const [profileName, setProfileName] = useState(getDisplayName() || getUsername() || 'User');
  const [profileCompany, setProfileCompany] = useState(getCompanyName() || '');
  const [profileMenuAnchor, setProfileMenuAnchor] = useState<null | HTMLElement>(null);
  const [profileDialogOpen, setProfileDialogOpen] = useState(false);

  const handleLogin = () => setLoggedIn(true);

  const handleLogout = () => {
    clearToken();
    setLoggedIn(false);
  };

  if (!loggedIn) {
    return <Login onLogin={handleLogin} />;
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', background: '#f5f5f5', m: 0, p: 0, width: '100vw', boxSizing: 'border-box', overflowX: 'hidden' }}>
      {/* Header */}
      <AppBar position="fixed" sx={{ zIndex: 1201 }} color="primary">
        <Toolbar>
          <DashboardIcon sx={{ mr: 2 }} />
          <Typography variant="h6" noWrap component="div" sx={{ flex: 1 }}>
            DCAS Admin
          </Typography>
          <Button
            color="inherit"
            startIcon={<RefreshIcon />}
            onClick={() => setRefreshKey(k => k + 1)}
            sx={{ textTransform: 'none', mr: 2 }}
          >
            Refresh
          </Button>
          <Tooltip title={`${profileName} · ${profileCompany}`} arrow>
            <Button
              color="inherit"
              onClick={e => setProfileMenuAnchor(e.currentTarget)}
              sx={{ textTransform: 'none', display: 'flex', alignItems: 'center', gap: 0.75, px: 1.5, borderRadius: 2 }}
            >
              <PersonIcon sx={{ fontSize: 22 }} />
              <Typography variant="body2" sx={{ fontWeight: 600 }}>{profileName}</Typography>
            </Button>
          </Tooltip>
          <Menu
            anchorEl={profileMenuAnchor}
            open={Boolean(profileMenuAnchor)}
            onClose={() => setProfileMenuAnchor(null)}
            transformOrigin={{ horizontal: 'right', vertical: 'top' }}
            anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
          >
            <MenuItem onClick={() => { setProfileMenuAnchor(null); setProfileDialogOpen(true); }}>
              <ListItemIcon><AccountCircleIcon fontSize="small" /></ListItemIcon>
              My Profile
            </MenuItem>
            <MenuItem onClick={() => { setProfileMenuAnchor(null); handleLogout(); }}>
              <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>
              Logout
            </MenuItem>
          </Menu>
          <UserProfileDialog
            open={profileDialogOpen}
            onClose={() => setProfileDialogOpen(false)}
            displayName={profileName}
            companyName={profileCompany}
            onProfileUpdated={(dn, cn) => { setProfileName(dn); setProfileCompany(cn); }}
          />
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
            <Box sx={{ display: selected === 'doctor' ? 'flex' : 'none', flexDirection: 'column', width: '100%' }}><DoctorCrud key={refreshKey} /></Box>
            <Box sx={{ display: selected === 'medical' ? 'flex' : 'none', flexDirection: 'column', width: '100%' }}><MedicalCrud key={refreshKey} /></Box>
            <Box sx={{ display: selected === 'product' ? 'flex' : 'none', flexDirection: 'column', width: '100%' }}><ProductCrud key={refreshKey} /></Box>
            <Box sx={{ display: selected === 'commission' ? 'flex' : 'none', flexDirection: 'column', width: '100%' }}><CommissionCrud key={refreshKey} /></Box>
            <Box sx={{ display: selected === 'bulk-upload' ? 'flex' : 'none', flexDirection: 'column', width: '100%' }}><BulkMasterUpload key={refreshKey} onSuccess={() => setRefreshKey(k => k + 1)} /></Box>
            <Box sx={{ display: selected === 'process-sales' ? 'flex' : 'none', flexDirection: 'column', width: '100%' }}><ProcessSalesFile onSuccess={() => setRefreshKey(k => k + 1)} /></Box>
            <Box sx={{ display: selected === 'sales-transactions' ? 'flex' : 'none', flexDirection: 'column', width: '100%' }}><SalesTransactions key={refreshKey} /></Box>
            <Box sx={{ display: selected === 'doctor-balances' ? 'flex' : 'none', flexDirection: 'column', width: '100%' }}><DoctorBalances key={refreshKey} /></Box>
            <Box sx={{ display: selected === 'doctor-transactions' ? 'flex' : 'none', flexDirection: 'column', width: '100%' }}><DoctorTransactions key={refreshKey} /></Box>
            <Box sx={{ display: selected === 'add-doctor-transaction' ? 'flex' : 'none', flexDirection: 'column', width: '100%' }}><AddDoctorTransaction key={refreshKey} onSuccess={() => setRefreshKey(k => k + 1)} /></Box>
            <Box sx={{ display: selected === 'sync-doctor-wallet' ? 'flex' : 'none', flexDirection: 'column', width: '100%' }}><SyncDoctorWallet key={refreshKey} /></Box>
            <Box sx={{ display: selected === 'change-password' ? 'flex' : 'none', flexDirection: 'column', width: '100%' }}><ChangePassword key={refreshKey} /></Box>
          </Box>
        </Box>
      </Box>
    </Box>
  );
}

export default App;
