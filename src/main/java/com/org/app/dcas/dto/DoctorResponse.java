package com.org.app.dcas.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.org.app.dcas.model.Company;
import com.org.app.dcas.model.MedicalMaster;
import com.org.app.dcas.model.DoctorMaster;
import java.util.List;

public class DoctorResponse {
    private Long id;
    private String name;
    private String specialization;
    private String phoneNumber;
    private String email;
    @JsonIgnore
    private Company company;
    private MedicalMaster medical;
    private List<ProductCommissionResponse> productCommissions;

    public DoctorResponse(DoctorMaster doctor, MedicalMaster medical, List<ProductCommissionResponse> commissions) {
        this.id = doctor.getDoctorId();
        this.name = doctor.getName();
        this.specialization = doctor.getSpecialization();
        this.phoneNumber = doctor.getPhoneNumber();
        this.email = doctor.getEmail();
        this.company = doctor.getCompany();
        this.medical = medical;
        this.productCommissions = commissions;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public MedicalMaster getMedical() { return medical; }
    public void setMedical(MedicalMaster medical) { this.medical = medical; }

    public List<ProductCommissionResponse> getProductCommissions() { return productCommissions; }
    public void setProductCommissions(List<ProductCommissionResponse> productCommissions) { this.productCommissions = productCommissions; }
}
