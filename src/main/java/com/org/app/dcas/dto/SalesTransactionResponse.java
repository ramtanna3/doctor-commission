package com.org.app.dcas.dto;

import com.org.app.dcas.model.*;
import java.time.format.DateTimeFormatter;

public class SalesTransactionResponse {
    public Long salesTransactionId;
    public String date;
    public String voucherNo;
    public MedicalResponse medical;
    public ProductResponse product;
    public DoctorResponse doctor;
    public String qty;
    public String amount;
    public java.math.BigDecimal commissionPercent;
    public java.math.BigDecimal commissionAmount;
    public String createdAt;
    public String updatedAt;
    public FileAuditResponse fileAudit;
    public Boolean isMatched;

    public static SalesTransactionResponse from(SalesTransaction tx) {
        SalesTransactionResponse resp = new SalesTransactionResponse();
        resp.salesTransactionId = tx.getSalesTransactionId();
        resp.date = tx.getDate() != null ? tx.getDate().toString() : null;
        resp.voucherNo = tx.getVoucherNo();
        resp.medical = tx.getMedical() != null ? MedicalResponse.from(tx.getMedical()) : null;
        resp.product = tx.getProduct() != null ? ProductResponse.from(tx.getProduct()) : null;
        resp.doctor = tx.getDoctor() != null ? DoctorResponse.from(tx.getDoctor()) : null;
        resp.qty = tx.getQty();
        resp.amount = tx.getAmount();
        resp.commissionPercent = tx.getCommissionPercent();
        resp.commissionAmount = tx.getCommissionAmount();
        resp.createdAt = tx.getCreatedAt() != null ? tx.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        resp.updatedAt = tx.getUpdatedAt() != null ? tx.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        resp.fileAudit = tx.getFileAudit() != null ? FileAuditResponse.from(tx.getFileAudit()) : null;
        resp.isMatched = tx.getIsMatched();
        return resp;
    }

    public static class MedicalResponse {
        public Long medicalId;
        public String name;
        public String address;
        public String phoneNumber;
        public String email;
        public Object territory;

        public static MedicalResponse from(MedicalMaster m) {
            MedicalResponse resp = new MedicalResponse();
            resp.medicalId = m.getMedicalId();
            resp.name = m.getName();
            resp.address = m.getAddress();
            resp.phoneNumber = m.getPhoneNumber();
            resp.email = m.getEmail();
            resp.territory = m.getTerritory();
            return resp;
        }
    }

    public static class ProductResponse {
        public Long productId;
        public String name;
        public String description;
        public java.math.BigDecimal defaultCommissionPercentage;
        public Boolean isActive;

        public static ProductResponse from(ProductMaster p) {
            ProductResponse resp = new ProductResponse();
            resp.productId = p.getProductId();
            resp.name = p.getName();
            resp.description = p.getDescription();
            resp.defaultCommissionPercentage = p.getDefaultCommissionPercentage();
            resp.isActive = p.getIsActive();
            return resp;
        }
    }

    public static class DoctorResponse {
        public Long doctorId;
        public String name;
        public String specialization;
        public String phoneNumber;
        public String email;
        public Object mr;

        public static DoctorResponse from(DoctorMaster d) {
            DoctorResponse resp = new DoctorResponse();
            resp.doctorId = d.getDoctorId();
            resp.name = d.getName();
            resp.specialization = d.getSpecialization();
            resp.phoneNumber = d.getPhoneNumber();
            resp.email = d.getEmail();
            resp.mr = d.getMr();
            return resp;
        }
    }

    public static class FileAuditResponse {
        public Long fileAuditId;
        public String fileName;

        public static FileAuditResponse from(FileAudit fa) {
            FileAuditResponse resp = new FileAuditResponse();
            resp.fileAuditId = fa.getFileAuditId();
            resp.fileName = fa.getFileName();
            return resp;
        }
    }
}
