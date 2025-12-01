package com.org.app.dcas.dto;

import com.org.app.dcas.model.DoctorMaster;

public class DoctorWalletBalanceResponse {
    private DoctorMaster doctor;
    private double balance;

    public DoctorWalletBalanceResponse(DoctorMaster doctor, double balance) {
        this.doctor = doctor;
        this.balance = balance;
    }

    public DoctorMaster getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorMaster doctor) {
        this.doctor = doctor;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
