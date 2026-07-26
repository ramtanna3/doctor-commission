package com.org.app.dcas.dto;

import java.util.List;

public class SalesTransactionDoctorAssignmentResult {
    private List<Long> assignedSalesTransactionIds;
    private List<Long> failedSalesTransactionIds;

    public SalesTransactionDoctorAssignmentResult() {
    }

    public SalesTransactionDoctorAssignmentResult(List<Long> assignedSalesTransactionIds, List<Long> failedSalesTransactionIds) {
        this.assignedSalesTransactionIds = assignedSalesTransactionIds;
        this.failedSalesTransactionIds = failedSalesTransactionIds;
    }

    public List<Long> getAssignedSalesTransactionIds() {
        return assignedSalesTransactionIds;
    }

    public void setAssignedSalesTransactionIds(List<Long> assignedSalesTransactionIds) {
        this.assignedSalesTransactionIds = assignedSalesTransactionIds;
    }

    public List<Long> getFailedSalesTransactionIds() {
        return failedSalesTransactionIds;
    }

    public void setFailedSalesTransactionIds(List<Long> failedSalesTransactionIds) {
        this.failedSalesTransactionIds = failedSalesTransactionIds;
    }
}
