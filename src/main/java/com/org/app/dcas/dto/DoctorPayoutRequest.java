package com.org.app.dcas.dto;

import java.util.List;

public class DoctorPayoutRequest {
    public static class DoctorBalance {
        public Long doctorId;
        public double balance;
    }
    private List<DoctorBalance> doctorBalances;

    public List<DoctorBalance> getDoctorBalances() { return doctorBalances; }
    public void setDoctorBalances(List<DoctorBalance> doctorBalances) { this.doctorBalances = doctorBalances; }
}
