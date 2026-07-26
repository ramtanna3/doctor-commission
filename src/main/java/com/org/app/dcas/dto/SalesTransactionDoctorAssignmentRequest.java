package com.org.app.dcas.dto;

import java.util.List;

public class SalesTransactionDoctorAssignmentRequest {
    private Long doctorId;
    private List<Long> salesTransactionIds;

    public SalesTransactionDoctorAssignmentRequest() {
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public List<Long> getSalesTransactionIds() {
        return salesTransactionIds;
    }

    public void setSalesTransactionIds(List<Long> salesTransactionIds) {
        this.salesTransactionIds = salesTransactionIds;
    }
}
