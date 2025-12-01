package com.org.app.dcas.dto;

import com.org.app.dcas.model.Distributor;
import java.util.List;

public class SalesTransactionListResponse {
    private Distributor distributor;
    private List<SalesTransactionResponse> transactions;

    public SalesTransactionListResponse(Distributor distributor, List<SalesTransactionResponse> transactions) {
        this.distributor = distributor;
        this.transactions = transactions;
    }

    public Distributor getDistributor() { return distributor; }
    public void setDistributor(Distributor distributor) { this.distributor = distributor; }

    public List<SalesTransactionResponse> getTransactions() { return transactions; }
    public void setTransactions(List<SalesTransactionResponse> transactions) { this.transactions = transactions; }
}
