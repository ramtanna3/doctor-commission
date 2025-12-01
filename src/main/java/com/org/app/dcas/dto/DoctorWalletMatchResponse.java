package com.org.app.dcas.dto;

import com.org.app.dcas.model.DoctorMaster;
import com.org.app.dcas.model.SalesTransaction;
import java.util.List;

public class DoctorWalletMatchResponse {
    private DoctorMaster doctor;
    private Double walletBalance;
    private List<TransactionMatch> transactions;

    public DoctorWalletMatchResponse(DoctorMaster doctor, Double walletBalance, List<TransactionMatch> transactions) {
        this.doctor = doctor;
        this.walletBalance = walletBalance;
        this.transactions = transactions;
    }

    public DoctorMaster getDoctor() { return doctor; }
    public void setDoctor(DoctorMaster doctor) { this.doctor = doctor; }

    public Double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(Double walletBalance) { this.walletBalance = walletBalance; }

    public List<TransactionMatch> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionMatch> transactions) { this.transactions = transactions; }

    public static class TransactionMatch {
        private SalesTransaction transaction;
        private boolean matched;

        public TransactionMatch(SalesTransaction transaction, boolean matched) {
            this.transaction = transaction;
            this.matched = matched;
        }

        public SalesTransaction getTransaction() { return transaction; }
        public void setTransaction(SalesTransaction transaction) { this.transaction = transaction; }

        public boolean isMatched() { return matched; }
        public void setMatched(boolean matched) { this.matched = matched; }
    }
}
