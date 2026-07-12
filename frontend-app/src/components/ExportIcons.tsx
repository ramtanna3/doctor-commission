import Box from '@mui/material/Box';

export function ExcelIcon({ size = 22, color = '#0f9d58' }: { size?: number; color?: string }) {
  return (
    <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M14 2H7a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8l-5-6Z" fill={color} />
        <path d="M14 2v6h6" stroke="#ffffff" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
        <path d="M8.5 13.5 12 17l3.5-3.5M12 17V10" stroke="#ffffff" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    </Box>
  );
}

export function PdfIcon({ size = 22, color = '#d93025' }: { size?: number; color?: string }) {
  return (
    <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}>
      <svg width={size} height={size} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M7 3h7l4 4v14a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z" fill={color} />
        <path d="M14 3v4h4" stroke="#ffffff" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
        <path d="M9 13h2.5a1.5 1.5 0 1 1 0 3H9v-3Z" fill="#ffffff" />
        <path d="M9 10.5h3.5a1.5 1.5 0 1 1 0 3H9v-3Z" fill="#ffffff" />
        <path d="M9 16.5h2.5a1.5 1.5 0 0 1 0 3H9v-3Z" fill="#ffffff" />
      </svg>
    </Box>
  );
}
