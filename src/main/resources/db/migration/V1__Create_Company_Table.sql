create schema dcas_db;

USE dcas_db;

-- =========================
--  COMPANY TABLE
-- =========================
CREATE TABLE company (
    company_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    is_active TINYINT(1) DEFAULT 1,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================
--  TERRITORY MASTER TABLE
-- =========================
CREATE TABLE territory_master (
    territory_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    is_active TINYINT(1) DEFAULT 1,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(company_id)
);

-- =========================
--  MR MASTER TABLE
-- =========================
CREATE TABLE mr_master (
    mr_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    territory_id BIGINT,
    phone_number VARCHAR(20),
    email VARCHAR(255),
    is_active TINYINT(1) DEFAULT 1,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(company_id),
    FOREIGN KEY (territory_id) REFERENCES territory_master(territory_id)
);

-- =========================
--  MEDICAL MASTER TABLE
-- =========================
CREATE TABLE medical_master (
    medical_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    territory_id BIGINT,
    is_active TINYINT(1) DEFAULT 1,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(company_id),
    FOREIGN KEY (territory_id) REFERENCES territory_master(territory_id)
);

-- =========================
--  DOCTOR MASTER TABLE
-- =========================
CREATE TABLE doctor_master (
    doctor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    specialization VARCHAR(255),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    mr_id BIGINT,
    is_active TINYINT(1) DEFAULT 1,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(company_id),
    FOREIGN KEY (mr_id) REFERENCES mr_master(mr_id)
);

-- =========================
--  PRODUCT MASTER TABLE
-- =========================
CREATE TABLE product_master (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    default_commission_percentage DECIMAL(5,2),
    is_active TINYINT(1) DEFAULT 1,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(company_id)
);

-- =========================
--  COMMISSION MASTER TABLE
-- =========================
CREATE TABLE commission_master (
    commission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    doctor_id BIGINT,
    medical_id BIGINT,
    product_id BIGINT,
    commission_percentage DECIMAL(5,2),
    is_active TINYINT(1) DEFAULT 1,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(company_id),
    FOREIGN KEY (doctor_id) REFERENCES doctor_master(doctor_id),
    FOREIGN KEY (medical_id) REFERENCES medical_master(medical_id),
    FOREIGN KEY (product_id) REFERENCES product_master(product_id)
);

-- =========================
--  DISTRIBUTOR TABLE
-- =========================
CREATE TABLE distributor (
    distributor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    distributor_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    email VARCHAR(255),
    company_id BIGINT NOT NULL,
    is_active TINYINT(1) DEFAULT 1,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(company_id)
);

-- =========================
--  USERS TABLE
-- =========================
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    is_active TINYINT(1) DEFAULT 1,
    company_id BIGINT NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company(company_id)
);

-- =========================
--  FILE AUDIT TABLE
-- =========================
CREATE TABLE file_audit (
    file_audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255),
    uploaded_by BIGINT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_rows INT,
    success_count INT,
    nigo_count INT,
    unmatched_count INT,
    error_count INT,
    errors LONGTEXT
);

-- =========================
--  SALES TRANSACTION TABLE
-- =========================
CREATE TABLE sales_transaction (
    sales_transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    distributor_id BIGINT NOT NULL,
    transaction_date DATE,
    voucher_no VARCHAR(255),
    medical_id BIGINT,
    product_id BIGINT,
    doctor_id BIGINT,
    qty VARCHAR(20),
    amount VARCHAR(50),
    commission_percent DECIMAL(5,2),
    commission_amount DECIMAL(10,2),
    file_audit_id BIGINT,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    raw_medical_name VARCHAR(255),
    raw_product_name VARCHAR(255),
    is_matched TINYINT(1),
    FOREIGN KEY (company_id) REFERENCES company(company_id),
    FOREIGN KEY (distributor_id) REFERENCES distributor(distributor_id),
    FOREIGN KEY (medical_id) REFERENCES medical_master(medical_id),
    FOREIGN KEY (product_id) REFERENCES product_master(product_id),
    FOREIGN KEY (doctor_id) REFERENCES doctor_master(doctor_id),
    FOREIGN KEY (file_audit_id) REFERENCES file_audit(file_audit_id)
);

-- =========================
--  NIGO SALES TRANSACTION TABLE
-- =========================
CREATE TABLE nigo_sales_transactions (
    nigo_sales_transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    distributor_id BIGINT,
    transaction_date DATE,
    voucher_no VARCHAR(255),
    medical_id BIGINT,
    product_id BIGINT,
    doctor_id BIGINT,
    qty VARCHAR(20),
    amount VARCHAR(50),
    commission_percent DECIMAL(5,2),
    commission_amount DECIMAL(10,2),
    raw_medical_name VARCHAR(255),
    raw_product_name VARCHAR(255),
    is_matched TINYINT(1),
    file_audit_id BIGINT,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    reason VARCHAR(255),
    FOREIGN KEY (company_id) REFERENCES company(company_id),
    FOREIGN KEY (distributor_id) REFERENCES distributor(distributor_id),
    FOREIGN KEY (medical_id) REFERENCES medical_master(medical_id),
    FOREIGN KEY (product_id) REFERENCES product_master(product_id),
    FOREIGN KEY (doctor_id) REFERENCES doctor_master(doctor_id),
    FOREIGN KEY (file_audit_id) REFERENCES file_audit(file_audit_id)
);

-- =========================
--  DOCTOR WALLET LEDGER TABLE
-- =========================
CREATE TABLE doctor_wallet_ledger (
    doctor_wallet_ledger_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    transaction_date DATE NOT NULL,
    reference_type ENUM('ADVANCE_CREDIT', 'SALE_COMMISSION', 'PAYOUT', 'ADJUSTMENT') NOT NULL,
    reference_id BIGINT NULL,
    sales_transaction_id BIGINT NULL,
    credit_amount DECIMAL(10,2) DEFAULT 0.00,
    debit_amount DECIMAL(10,2) DEFAULT 0.00,
    remarks VARCHAR(255),
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES doctor_master(doctor_id),
    FOREIGN KEY (sales_transaction_id) REFERENCES sales_transaction(sales_transaction_id)
);

-- All tables now have created_by and updated_by as VARCHAR(100)

INSERT INTO company (name, address, phone_number, email, is_active) VALUES
  ('Acme Pharma', '123 Main St', '1234567890', 'info@acme.com', 1),
  ('Beta Pharma', '456 Side St', '9876543210', 'contact@beta.com', 1),
  ('Inactive Pharma', '789 Off Rd', '1112223333', 'inactive@pharma.com', 0);


-- USERS
INSERT INTO users (first_name, last_name, phone_number, email, is_active, company_id) VALUES
  ('Admin', 'Acme', '8000011111', 'admin@acme.com', 1, 1),
  ('User', 'Beta', '8000022222', 'user@beta.com', 1, 2),
  ('Inactive', 'User', '8000033333', 'inactive@pharma.com', 0, 3);
