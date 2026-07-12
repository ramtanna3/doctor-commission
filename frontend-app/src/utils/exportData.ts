import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';

export interface ExportFilterState {
  search: string;
  statusFilter: string[];
  dateFrom: string;
  dateTo: string;
}

export function buildVisibleTransactionsForExport(
  transactions: any[],
  filters: ExportFilterState,
) {
  const { search, statusFilter, dateFrom, dateTo } = filters;

  return transactions.filter((t: any) => {
    const isMatched = t.isMatched === true;
    if (!statusFilter.includes('matched') && isMatched) return false;
    if (!statusFilter.includes('unmatched') && !isMatched) return false;

    const dateStr = t.date || t.transactionDate || '';
    if (dateFrom && dateStr && dateStr < dateFrom) return false;
    if (dateTo && dateStr && dateStr > dateTo) return false;

    if (!search) return true;

    const text = [
      t.salesTransactionId || t.transactionId || t.id,
      dateStr,
      t.doctor?.name || '',
      t.medical?.name || '',
      t.product?.name || '',
      t.qty || t.quantity,
      t.amount,
      t.commissionPercent,
      t.commissionAmount,
      t.status || (t.isMatched === false ? 'Unmatched' : 'Matched'),
    ]
      .join(' ')
      .toLowerCase();

    return text.includes(search.toLowerCase());
  });
}

function normalizeCell(value: unknown) {
  if (value === null || value === undefined || value === '') return '';
  return String(value);
}

export function exportTransactionsToExcel(
  transactions: any[],
  distributorName: string,
) {
  const rows = transactions.map((t) => ({
    Date: normalizeCell(t.date || t.transactionDate || ''),
    'Voucher ID': normalizeCell(t.voucherNo || ''),
    Doctor: normalizeCell(t.doctor?.name || ''),
    Medical: normalizeCell(t.medical?.name || ''),
    Product: normalizeCell(t.product?.name || ''),
    Quantity: normalizeCell(t.qty || t.quantity || ''),
    Amount: normalizeCell(t.amount ?? ''),
    'Promotional %': normalizeCell(t.commissionPercent ?? ''),
    'Promotional Amount': normalizeCell(t.commissionAmount ?? ''),
    Status: normalizeCell(t.status || (t.isMatched === false ? 'Unmatched' : 'Matched')),
  }));

  const worksheet = XLSX.utils.json_to_sheet(rows);
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Sales Transactions');

  const safeName = (distributorName || 'sales-transactions').replace(/[^a-z0-9]+/gi, '-').toLowerCase();
  XLSX.writeFile(workbook, `${safeName}.xlsx`);
}

export function exportTransactionsToPdf(
  transactions: any[],
  distributorName: string,
) {
  const doc = new jsPDF({ orientation: 'landscape' });
  doc.setFontSize(16);
  doc.text(`Sales Transactions - ${distributorName || 'All Distributors'}`, 14, 16);

  const rows = transactions.map((t) => [
    normalizeCell(t.date || t.transactionDate || ''),
    normalizeCell(t.voucherNo || ''),
    normalizeCell(t.doctor?.name || ''),
    normalizeCell(t.medical?.name || ''),
    normalizeCell(t.product?.name || ''),
    normalizeCell(t.qty || t.quantity || ''),
    normalizeCell(t.amount ?? ''),
    normalizeCell(t.commissionPercent ?? ''),
    normalizeCell(t.commissionAmount ?? ''),
    normalizeCell(t.status || (t.isMatched === false ? 'Unmatched' : 'Matched')),
  ]);

  autoTable(doc, {
    head: [[
      'Date',
      'Voucher ID',
      'Doctor',
      'Medical',
      'Product',
      'Quantity',
      'Amount',
      'Promotional %',
      'Promotional Amount',
      'Status',
    ]],
    body: rows,
    startY: 24,
    styles: { fontSize: 8 },
    headStyles: { fillColor: [25, 118, 210] },
  });

  const safeName = (distributorName || 'sales-transactions').replace(/[^a-z0-9]+/gi, '-').toLowerCase();
  doc.save(`${safeName}.pdf`);
}
