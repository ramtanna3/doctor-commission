package com.org.app.dcas.dto;

import java.time.LocalDate;

public class SalesExcelRow {
    private Long distributorId;
    private LocalDate date; // changed from String to LocalDate
    private String voucherNo;
    private String medical;
    private String productName;
    private Integer qty;
    private String doctor;
    private Double amount;

    // Getters and setters
    public Long getDistributorId() { return distributorId; }
    public void setDistributorId(Long distributorId) { this.distributorId = distributorId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getVoucherNo() { return voucherNo; }
    public void setVoucherNo(String voucherNo) { this.voucherNo = voucherNo; }

    public String getMedical() { return medical; }
    public void setMedical(String medical) { this.medical = medical; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }

    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
