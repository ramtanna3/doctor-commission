package com.org.app.dcas.service;

import com.org.app.dcas.model.DoctorMaster;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.DoctorMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DoctorService {

    private final DoctorMasterRepository doctorMasterRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public DoctorService(DoctorMasterRepository doctorMasterRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.doctorMasterRepository = doctorMasterRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<DoctorMaster> getAllDoctorsForCurrentCompany() {
        return doctorMasterRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
    }

    public List<DoctorMaster> getActiveDoctorsForCurrentCompany() {
        return doctorMasterRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
    }

    public DoctorMaster getDoctorByIdForCurrentCompany(Long doctorId) {
        return doctorMasterRepository.findByDoctorIdAndCompanyId(doctorId, companyContext.getCompanyId()).orElse(null);
    }

    public DoctorMaster createDoctorForCurrentCompany(DoctorMaster doctor) {
        Long companyId = companyContext.getCompanyId();
        doctor.setCompany(companyRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        // Set createdBy/updatedBy from context
        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
        doctor.setCreatedBy(userIdStr);
        doctor.setUpdatedBy(userIdStr);
        return doctorMasterRepository.save(doctor);
    }

    public DoctorMaster updateDoctorForCurrentCompany(Long doctorId, DoctorMaster doctor) {
        return doctorMasterRepository.findByDoctorIdAndCompanyId(doctorId, companyContext.getCompanyId()).map(existing -> {
            existing.setName(doctor.getName());
            existing.setSpecialization(doctor.getSpecialization());
            existing.setPhoneNumber(doctor.getPhoneNumber());
            existing.setEmail(doctor.getEmail());
            existing.setMr(doctor.getMr());
            // Set updatedBy from context
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            return doctorMasterRepository.save(existing);
        }).orElse(null);
    }

    public boolean softDeleteDoctorForCurrentCompany(Long doctorId) {
        return doctorMasterRepository.findByDoctorIdAndCompanyId(doctorId, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            // Set updatedBy from context
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            doctorMasterRepository.save(existing);
            return true;
        }).orElse(false);
    }
}

