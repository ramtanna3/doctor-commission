package com.org.app.dcas.dto;

import com.org.app.dcas.model.DoctorMaster;
import java.util.List;

public class DoctorWalletLedgerListResponse {
    private DoctorMaster doctor;
    private List<DoctorWalletLedgerResponse> transactions;

    public DoctorWalletLedgerListResponse(DoctorMaster doctor, List<DoctorWalletLedgerResponse> transactions) {
        this.doctor = doctor;
        this.transactions = transactions;
    }

    public DoctorMaster getDoctor() { return doctor; }
    public void setDoctor(DoctorMaster doctor) { this.doctor = doctor; }

    public List<DoctorWalletLedgerResponse> getTransactions() { return transactions; }
    public void setTransactions(List<DoctorWalletLedgerResponse> transactions) { this.transactions = transactions; }
}
