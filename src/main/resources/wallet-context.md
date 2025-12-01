## 🧩 **Context: Doctor Wallet Ledger – Final Design and Logic**

The **Doctor Wallet Ledger** maintains a continuous financial record for each doctor, reflecting all movements such as advances, sales commissions, payouts, and manual adjustments.

It serves as a **single source of truth** for a doctor’s current balance — whether the doctor has advance funds available or is owed money by the company.

---

### 🎯 **Core Principle**

* The wallet tracks the **net financial position** between the company and each doctor.
* **Positive balance (+)** → Doctor has advance credit (future commissions will be adjusted).
* **Zero (0)** → Fully settled.
* **Negative balance (-)** → Company owes the doctor (doctor has earned more than credited).

---

### 🏗️ **Table Definition**

```sql
CREATE TABLE doctor_wallet_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    transaction_date DATE NOT NULL,
    reference_type ENUM('ADVANCE_CREDIT', 'SALE_COMMISSION', 'PAYOUT', 'ADJUSTMENT') NOT NULL,
    reference_id BIGINT NULL,  -- link to sales_transaction.id or manual entry reference
    credit_amount DECIMAL(10,2) DEFAULT 0.00,  -- adds to wallet (advance or payout)
    debit_amount DECIMAL(10,2) DEFAULT 0.00,   -- reduces wallet (commission earned)
    remarks VARCHAR(255),
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES doctor_master(id)
);
```

---

### ⚙️ **Action Mapping**

| **Action**            | **When It Happens**                   | **credit_amount** | **debit_amount**  | **reference_type** | **Wallet Effect** | **Meaning**                                           |
| --------------------- | ------------------------------------- | ----------------- | ----------------- | ------------------ | ----------------- | ----------------------------------------------------- |
| **Advance Given**     | When company gives advance to doctor  | advance_amount    | 0.00              | ADVANCE_CREDIT     | ➕ increases       | Doctor has extra funds in wallet                      |
| **Commission Earned** | When doctor earns commission via sale | 0.00              | commission_amount | SALE_COMMISSION    | ➖ decreases       | Doctor’s commissions reduce advance or create payable |
| **Payout Made**       | When company pays doctor manually     | payout_amount     | 0.00              | PAYOUT             | ➕ increases       | Company settles dues; brings wallet closer to zero    |
| **Adjustment**        | Manual correction (increase/decrease) | variable          | variable          | ADJUSTMENT         | depends           | Used for exceptional adjustments                      |

---

### 🧮 **Wallet Calculation Formula**

```sql
wallet_balance = SUM(credit_amount) - SUM(debit_amount)
```

* **Positive (+)** → Advance still available
* **Zero (0)** → Fully settled
* **Negative (-)** → Doctor is owed commission

---

### 📊 **Example Scenarios**

#### **Scenario 1 – With Advance**

| Action            | Amount | credit | debit | Wallet |
| ----------------- | ------ | ------ | ----- | ------ |
| Advance given     | ₹1000  | 1000   | 0     | +1000  |
| Commission earned | ₹300   | 0      | 300   | +700   |
| Commission earned | ₹1000  | 0      | 1000  | -300   |
| Payout made       | ₹300   | 300    | 0     | 0      |

✅ Final balance = **0** → Fully settled.

---

#### **Scenario 2 – No Advance**

| Action            | Amount | credit | debit | Wallet |
| ----------------- | ------ | ------ | ----- | ------ |
| Commission earned | ₹500   | 0      | 500   | -500   |
| Payout made       | ₹500   | 500    | 0     | 0      |

✅ Final balance = **0** → Fully settled.
Doctor earned ₹500, got ₹500 payout.

---

### 📈 **Balance Query**

```sql
SELECT 
    doctor_id,
    SUM(credit_amount) - SUM(debit_amount) AS wallet_balance
FROM doctor_wallet_ledger
GROUP BY doctor_id;
```

---

### 🚀 **Application Usage**

* Each **sale commission posting** creates a `SALE_COMMISSION` ledger entry.
* Each **advance payment** or **payout** creates a manual ledger entry (`ADVANCE_CREDIT` or `PAYOUT`).
* **Wallet balance** is never stored — always derived dynamically using the above formula.
* This enables a **transparent, auditable, and real-time** tracking of every doctor’s financial position.

---

Would you like me to now extend this with **API contract definitions** (e.g., `/api/doctor-wallet/add-transaction`, `/api/doctor-wallet/balance/:doctorId`) for your backend integration?
