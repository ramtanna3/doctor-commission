package com.org.app.dcas.dto;

import com.org.app.dcas.model.DoctorWalletLedger.ReferenceType;
import java.math.BigDecimal;

public class DoctorWalletTransactionRequest {
    private Long doctorId;
    private ReferenceType referenceType;
    private BigDecimal creditAmount;
    private BigDecimal debitAmount;
    private String remarks;
    private Long referenceId;

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public ReferenceType getReferenceType() { return referenceType; }
    public void setReferenceType(ReferenceType referenceType) { this.referenceType = referenceType; }

    public BigDecimal getCreditAmount() { return creditAmount; }
    public void setCreditAmount(BigDecimal creditAmount) { this.creditAmount = creditAmount; }

    public BigDecimal getDebitAmount() { return debitAmount; }
    public void setDebitAmount(BigDecimal debitAmount) { this.debitAmount = debitAmount; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
}
