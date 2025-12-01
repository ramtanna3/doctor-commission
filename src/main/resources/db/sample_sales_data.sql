-- COMPANY
INSERT INTO company (id, name, address, phone_number, email, is_active) VALUES
  (1, 'Acme Pharma', '123 Main St', '1234567890', 'info@acme.com', 1);

-- DISTRIBUTOR
INSERT INTO distributor (id, distributor_name, phone_number, email, company_id, territory_id, is_active) VALUES
  (1, 'North Distributors', '9000011111', 'north@distrib.com', 1, 1, 1);

-- TERRITORY
INSERT INTO territory_master (id, company_id, name, description, city, state, pincode, is_active) VALUES
  (1, 1, 'North Zone', 'Northern region', 'Delhi', 'Delhi', '110001', 1);

-- MEDICAL MASTER
INSERT INTO medical_master (id, company_id, name, address, phone_number, email, territory_id, is_active) VALUES
  (1, 1, 'DHANSHREE MEDICO CHEMIST &DRUG GIST  .MILKAT NO568, S. NO.142', '456 Health Ave', '1122334455', 'clinic@city.com', 1, 1),
  (2, 1, 'CITY CLINIC', '789 Metro Rd', '2233445566', 'metro@city.com', 1, 1),
  (3, 1, 'METRO HOSPITAL', '101 Beta Lane', '3344556677', 'beta@medical.com', 1, 1),
  (4, 1, 'SUNRISE MEDICAL', '202 Sunrise St', '4455667788', 'sunrise@medical.com', 1, 1);

-- PRODUCT MASTER
INSERT INTO product_master (id, company_id, name, description, default_commission_percentage, is_active) VALUES
  (1, 1, 'NURACONAZOLE 200 CAP', 'Antifungal capsule', 10.00, 1);

-- DOCTOR MASTER
INSERT INTO doctor_master (id, company_id, name, specialization, phone_number, email, mr_id, is_active) VALUES
  (1, 1, 'DR.GIRISH GAIKWAD', 'General', '9988776655', 'dr.girish@acme.com', 1, 1),
  (2, 1, 'DR.ANITA SHARMA', 'Cardiology', '9988776656', 'dr.anita@acme.com', 1, 1),
  (3, 1, 'DR.RAJESH KUMAR', 'Neurology', '9988776657', 'dr.rajesh@acme.com', 1, 1),
  (4, 1, 'DR.SUNIL PATIL', 'Orthopedics', '9988776658', 'dr.sunil@acme.com', 1, 1);

-- COMMISSION MASTER
INSERT INTO commission_master (id, company_id, doctor_id, medical_id, product_id, commission_percentage, is_active) VALUES
  (1, 1, 1, 1, 1, 10.00, 1);

-- SALES TRANSACTION (multiple sample rows from Excel)
INSERT INTO sales_transaction (
    distributor_id, transaction_date, voucher_no, medical_id, product_id, doctor_id, qty, amount, commission_percent, commission_amount, created_at, updated_at
) VALUES
    (1, '2025-09-30', '9567', 1, 1, 1, '10', '1514.3', 10.00, 151.43, NOW(), NOW()),
    (1, '2025-09-30', '9568', 1, 1, 1, '5', '757.15', 10.00, 75.715, NOW(), NOW()),
    (1, '2025-09-30', '9569', 1, 1, 1, '8', '1211.44', 10.00, 121.144, NOW(), NOW()),
    (1, '2025-09-30', '9570', 1, 1, 1, '12', '1817.16', 10.00, 181.716, NOW(), NOW());
