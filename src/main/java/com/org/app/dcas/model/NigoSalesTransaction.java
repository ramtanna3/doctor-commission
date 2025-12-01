package com.org.app.dcas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nigo_sales_transactions")
public class NigoSalesTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nigo_sales_transaction_id")
    private Long nigoSalesTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distributor_id")
    private Distributor distributor;

    @Column(name = "transaction_date")
    private LocalDate date;

    @Column(name = "voucher_no")
    private String voucherNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_id")
    private MedicalMaster medical;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductMaster product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private DoctorMaster doctor;

    @Column(name = "qty")
    private String qty;

    @Column(name = "amount")
    private String amount;

    @Column(name = "commission_percent")
    private BigDecimal commissionPercent;

    @Column(name = "commission_amount")
    private BigDecimal commissionAmount;

    @Column(name = "raw_medical_name")
    private String rawMedicalName;

    @Column(name = "raw_product_name")
    private String rawProductName;

    @Column(name = "is_matched")
    private Boolean isMatched;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_audit_id")
    private FileAudit fileAudit;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reason")
    private String reason;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return nigoSalesTransactionId;
    }

    public Long getNigoSalesTransactionId() { return nigoSalesTransactionId; }
    public void setNigoSalesTransactionId(Long nigoSalesTransactionId) { this.nigoSalesTransactionId = nigoSalesTransactionId; }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Distributor getDistributor() {
        return distributor;
    }

    public void setDistributor(Distributor distributor) {
        this.distributor = distributor;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getVoucherNo() {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    public MedicalMaster getMedical() {
        return medical;
    }

    public void setMedical(MedicalMaster medical) {
        this.medical = medical;
    }

    public ProductMaster getProduct() {
        return product;
    }

    public void setProduct(ProductMaster product) {
        this.product = product;
    }

    public DoctorMaster getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorMaster doctor) {
        this.doctor = doctor;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public BigDecimal getCommissionPercent() {
        return commissionPercent;
    }

    public void setCommissionPercent(BigDecimal commissionPercent) {
        this.commissionPercent = commissionPercent;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public String getRawMedicalName() {
        return rawMedicalName;
    }

    public void setRawMedicalName(String rawMedicalName) {
        this.rawMedicalName = rawMedicalName;
    }

    public String getRawProductName() {
        return rawProductName;
    }

    public void setRawProductName(String rawProductName) {
        this.rawProductName = rawProductName;
    }

    public Boolean getMatched() {
        return isMatched;
    }

    public void setMatched(Boolean matched) {
        isMatched = matched;
    }

    public FileAudit getFileAudit() {
        return fileAudit;
    }

    public void setFileAudit(FileAudit fileAudit) {
        this.fileAudit = fileAudit;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}