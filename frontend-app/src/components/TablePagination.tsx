import React from 'react';
import Pagination from '@mui/material/Pagination';
import Stack from '@mui/material/Stack';

export interface TablePaginationProps {
  count: number;
  page: number;
  rowsPerPage: number;
  onPageChange: (event: React.ChangeEvent<unknown>, value: number) => void;
}

const TablePagination: React.FC<TablePaginationProps> = ({ count, page, rowsPerPage, onPageChange }) => {
  const pageCount = Math.ceil(count / rowsPerPage);
  return (
    <Stack spacing={2} alignItems="center" sx={{ my: 2 }}>
      <Pagination
        count={pageCount}
        page={page}
        onChange={onPageChange}
        color="primary"
        shape="rounded"
        showFirstButton
        showLastButton
      />
    </Stack>
  );
};

export default TablePagination;
