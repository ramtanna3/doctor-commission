-- COMPANY
INSERT INTO company (name, address, phone_number, email, is_active) VALUES
  ('Acme Pharma', '123 Main St', '1234567890', 'info@acme.com', 1),
  ('Beta Pharma', '456 Side St', '9876543210', 'contact@beta.com', 1),
  ('Inactive Pharma', '789 Off Rd', '1112223333', 'inactive@pharma.com', 0);

-- TERRITORY
INSERT INTO territory_master (company_id, name, description, city, state, pincode, is_active) VALUES
  (1, 'North Zone', 'Northern region', 'Delhi', 'Delhi', '110001', 1),
  (1, 'South Zone', 'Southern region', 'Chennai', 'Tamil Nadu', '600001', 1),
  (2, 'West Zone', 'Western region', 'Mumbai', 'Maharashtra', '400001', 1);

-- MR MASTER
INSERT INTO mr_master (company_id, name, territory_id, phone_number, email, is_active) VALUES
  (1, 'John Doe', 1, '9876543210', 'john.doe@acme.com', 1),
  (1, 'Jane Smith', 2, '9988776655', 'jane.smith@acme.com', 1),
  (2, 'Beta Rep', 3, '9123456780', 'rep@beta.com', 1);

-- MEDICAL MASTER
INSERT INTO medical_master (company_id, name, address, phone_number, email, territory_id, is_active) VALUES
  (1, 'City Clinic', '456 Health Ave', '1122334455', 'clinic@city.com', 1, 1),
  (1, 'Metro Hospital', '789 Metro Rd', '2233445566', 'metro@city.com', 2, 1),
  (2, 'Beta Medical', '101 Beta Lane', '3344556677', 'beta@medical.com', 3, 1);

-- DOCTOR MASTER
INSERT INTO doctor_master (company_id, name, specialization, phone_number, email, mr_id, is_active) VALUES
  (1, 'Dr. Smith', 'Cardiology', '9988776655', 'dr.smith@acme.com', 1, 1),
  (1, 'Dr. Alice', 'Neurology', '8877665544', 'dr.alice@acme.com', 2, 1),
  (2, 'Dr. Beta', 'Orthopedics', '7766554433', 'dr.beta@beta.com', 3, 1);

-- PRODUCT MASTER
INSERT INTO product_master (company_id, name, description, default_commission_percentage, is_active) VALUES
  (1, 'HeartCare', 'Cardiac support medicine', 10.00, 1),
  (1, 'NeuroPlus', 'Neurology support medicine', 12.50, 1),
  (2, 'BetaCure', 'General medicine', 8.00, 1);

-- COMMISSION MASTER
INSERT INTO commission_master (company_id, doctor_id, medical_id, product_id, commission_percentage, is_active) VALUES
  (1, 1, 1, 1, 10.00, 1),
  (1, 2, 2, 2, 12.50, 1),
  (2, 3, 3, 3, 8.00, 1);

-- DISTRIBUTOR
INSERT INTO distributor (distributor_name, phone_number, email, company_id, territory_id, is_active) VALUES
  ('North Distributors', '9000011111', 'north@distrib.com', 1, 1, 1),
  ('South Distributors', '9000022222', 'south@distrib.com', 1, 2, 1),
  ('Beta Distributors', '9000033333', 'beta@distrib.com', 2, 3, 1);

-- USERS
INSERT INTO users (first_name, last_name, phone_number, email, is_active, company_id) VALUES
  ('Admin', 'Acme', '8000011111', 'admin@acme.com', 1, 1),
  ('User', 'Beta', '8000022222', 'user@beta.com', 1, 2),
  ('Inactive', 'User', '8000033333', 'inactive@pharma.com', 0, 3);
