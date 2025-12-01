package com.org.app.dcas.dto;

import com.org.app.dcas.model.DoctorWalletLedger;
import java.time.format.DateTimeFormatter;

public class DoctorWalletLedgerResponse {
    public String productName;
    public Long doctorWalletLedgerId;
    public String transactionDate;
    public String referenceType;
    public Long referenceId;
    public double creditAmount;
    public double debitAmount;
    public String remarks;
    public String createdAt;
    public Long id;

    public static DoctorWalletLedgerResponse from(DoctorWalletLedger ledger) {
        DoctorWalletLedgerResponse resp = new DoctorWalletLedgerResponse();
        resp.doctorWalletLedgerId = ledger.getDoctorWalletLedgerId();
        resp.transactionDate = ledger.getTransactionDate() != null ? ledger.getTransactionDate().toString() : null;
        resp.referenceType = ledger.getReferenceType() != null ? ledger.getReferenceType().name() : null;
        resp.referenceId = ledger.getReferenceId();
        resp.creditAmount = ledger.getCreditAmount() != null ? ledger.getCreditAmount().doubleValue() : 0.0;
        resp.debitAmount = ledger.getDebitAmount() != null ? ledger.getDebitAmount().doubleValue() : 0.0;
        resp.remarks = ledger.getRemarks();
        resp.createdAt = ledger.getCreatedAt() != null ? ledger.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        resp.id = ledger.getDoctorWalletLedgerId();
        // Set productName from salesTransaction if available
        if (ledger.getSalesTransaction() != null && ledger.getSalesTransaction().getProduct() != null) {
            resp.productName = ledger.getSalesTransaction().getProduct().getName();
        } else {
            resp.productName = null;
        }
        return resp;
    }
}
