import React from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';

export interface Column {
  id: string;
  label: string;
  minWidth?: number;
  align?: 'right' | 'left' | 'center';
  format?: (value: any) => string;
}

export interface DataTableProps<T> {
  columns: Column[];
  rows: T[];
  getRowKey: (row: T) => string | number;
  renderActions?: (row: T) => React.ReactNode;
}

function DataTable<T>({ columns, rows, getRowKey, renderActions }: DataTableProps<T>) {
  return (
    <TableContainer component={Paper}>
      <Table size="small">
        <TableHead>
          <TableRow>
            {columns.map(col => (
              <TableCell key={col.id} align={col.align} style={{ minWidth: col.minWidth }}>
                {col.label}
              </TableCell>
            ))}
            {renderActions && <TableCell>Actions</TableCell>}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map(row => (
            <TableRow key={getRowKey(row)}>
              {columns.map(col => (
                <TableCell key={col.id} align={col.align}>
                  {col.format ? col.format((row as any)[col.id]) : (row as any)[col.id]}
                </TableCell>
              ))}
              {renderActions && <TableCell>{renderActions(row)}</TableCell>}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}

export default DataTable;
