import { describe, expect, it } from 'vitest';
import { buildVisibleTransactionsForExport } from './exportData';

describe('buildVisibleTransactionsForExport', () => {
  it('returns only rows matching the current filters', () => {
    const transactions = [
      {
        id: 1,
        date: '2026-01-15',
        voucherNo: 'V1',
        doctor: { name: 'Dr. A' },
        medical: { name: 'MediOne' },
        product: { name: 'Product X' },
        qty: 2,
        amount: 100,
        commissionPercent: 10,
        commissionAmount: 10,
        isMatched: true,
      },
      {
        id: 2,
        date: '2026-01-16',
        voucherNo: 'V2',
        doctor: { name: 'Dr. B' },
        medical: { name: 'MediTwo' },
        product: { name: 'Product Y' },
        qty: 3,
        amount: 200,
        commissionPercent: 12,
        commissionAmount: 24,
        isMatched: false,
      },
    ];

    const result = buildVisibleTransactionsForExport(transactions, {
      search: 'dr. a',
      statusFilter: ['matched'],
      dateFrom: '',
      dateTo: '',
    });

    expect(result).toHaveLength(1);
    expect(result[0].id).toBe(1);
  });
});
