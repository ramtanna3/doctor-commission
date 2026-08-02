-- =================================================================
--  V2: Performance Indexes
--  All queries use WHERE company_id = ? AND is_active = 1 pattern.
--  Composite (company_id, is_active) lets MySQL satisfy both
--  conditions using one index scan instead of a company_id scan
--  followed by a full-row filter for is_active.
-- =================================================================

-- Master tables: composite (company_id, is_active)
CREATE INDEX idx_doctor_master_company_active   ON doctor_master   (company_id, is_active);
CREATE INDEX idx_medical_master_company_active  ON medical_master  (company_id, is_active);
CREATE INDEX idx_product_master_company_active  ON product_master  (company_id, is_active);
CREATE INDEX idx_commission_master_company_active ON commission_master (company_id, is_active);

-- Sales transactions: distributor + match-status filter
CREATE INDEX idx_sales_txn_distributor_matched  ON sales_transaction (distributor_id, is_matched);

-- Sales transactions: company + doctor lookup
CREATE INDEX idx_sales_txn_company_doctor       ON sales_transaction (company_id, doctor_id);

-- Doctor wallet ledger: date range queries per doctor
CREATE INDEX idx_dwl_doctor_date                ON doctor_wallet_ledger (doctor_id, transaction_date);
