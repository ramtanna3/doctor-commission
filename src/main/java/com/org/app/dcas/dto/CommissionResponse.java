// ...filepath: /Users/ramtanna/git_ramtanna3/doctor-commision-automation/dcas/src/main/java/com/org/app/dcas/dto/CommissionResponse.java
package com.org.app.dcas.dto;

import com.org.app.dcas.model.*;

public class CommissionResponse {
    public Long commissionId;
    public DoctorResponse doctor;
    public MedicalResponse medical;
    public ProductResponse product;
    public java.math.BigDecimal commissionPercentage;
    public Boolean isActive;

    public static CommissionResponse from(CommissionMaster cm) {
        CommissionResponse resp = new CommissionResponse();
        resp.commissionId = cm.getCommissionId();
        resp.doctor = cm.getDoctor() != null ? DoctorResponse.from(cm.getDoctor()) : null;
        resp.medical = cm.getMedical() != null ? MedicalResponse.from(cm.getMedical()) : null;
        resp.product = cm.getProduct() != null ? ProductResponse.from(cm.getProduct()) : null;
        resp.commissionPercentage = cm.getCommissionPercentage();
        resp.isActive = cm.getIsActive();
        return resp;
    }

    public static class DoctorResponse {
        public Long doctorId;
        public String name;
        public String specialization;
        public String phoneNumber;
        public String email;
        public Object mr;
        public Boolean isActive;

        public static DoctorResponse from(DoctorMaster d) {
            DoctorResponse resp = new DoctorResponse();
            resp.doctorId = d.getDoctorId();
            resp.name = d.getName();
            resp.specialization = d.getSpecialization();
            resp.phoneNumber = d.getPhoneNumber();
            resp.email = d.getEmail();
            resp.mr = d.getMr();
            resp.isActive = d.getIsActive();
            return resp;
        }
    }

    public static class MedicalResponse {
        public Long medicalId;
        public String name;
        public String address;
        public String phoneNumber;
        public String email;
        public Object territory;
        public Boolean isActive;

        public static MedicalResponse from(MedicalMaster m) {
            MedicalResponse resp = new MedicalResponse();
            resp.medicalId = m.getMedicalId();
            resp.name = m.getName();
            resp.address = m.getAddress();
            resp.phoneNumber = m.getPhoneNumber();
            resp.email = m.getEmail();
            resp.territory = m.getTerritory();
            resp.isActive = m.getIsActive();
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
}
