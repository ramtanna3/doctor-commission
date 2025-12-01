package com.org.app.dcas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_wallet_ledger")
public class DoctorWalletLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_wallet_ledger_id")
    private Long doctorWalletLedgerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorMaster doctor;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false)
    private ReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    // New: direct FK to sales_transaction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_transaction_id", referencedColumnName = "sales_transaction_id")
    private SalesTransaction salesTransaction;

    public SalesTransaction getSalesTransaction() {
        return salesTransaction;
    }
    public void setSalesTransaction(SalesTransaction salesTransaction) {
        this.salesTransaction = salesTransaction;
        if (salesTransaction != null) {
            this.referenceId = salesTransaction.getSalesTransactionId();
        }
    }

    @Column(name = "credit_amount", nullable = false)
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Column(name = "debit_amount", nullable = false)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ReferenceType {
        ADVANCE_CREDIT, SALE_COMMISSION, PAYOUT, ADJUSTMENT
    }

    // Getters and setters
    public Long getDoctorWalletLedgerId() { return doctorWalletLedgerId; }
    public void setDoctorWalletLedgerId(Long doctorWalletLedgerId) { this.doctorWalletLedgerId = doctorWalletLedgerId; }

    public DoctorMaster getDoctor() { return doctor; }
    public void setDoctor(DoctorMaster doctor) { this.doctor = doctor; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public ReferenceType getReferenceType() { return referenceType; }
    public void setReferenceType(ReferenceType referenceType) { this.referenceType = referenceType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public BigDecimal getCreditAmount() { return creditAmount; }
    public void setCreditAmount(BigDecimal creditAmount) { this.creditAmount = creditAmount; }

    public BigDecimal getDebitAmount() { return debitAmount; }
    public void setDebitAmount(BigDecimal debitAmount) { this.debitAmount = debitAmount; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getId() {
        return doctorWalletLedgerId;
    }
}