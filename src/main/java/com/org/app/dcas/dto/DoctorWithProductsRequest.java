package com.org.app.dcas.dto;

import java.util.List;

public class DoctorWithProductsRequest {
    private String name;
    private String specialization;
    private String phoneNumber;
    private String email;
    private Long medicalId;
    private List<ProductCommission> productCommissions;


    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getMedicalId() { return medicalId; }
    public void setMedicalId(Long medicalId) { this.medicalId = medicalId; }

    public List<ProductCommission> getProductCommissions() { return productCommissions; }
    public void setProductCommissions(List<ProductCommission> productCommissions) { this.productCommissions = productCommissions; }

}
