package com.org.app.dcas.service;

import com.org.app.dcas.model.MedicalMaster;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.MedicalMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicalMasterService {

    private final MedicalMasterRepository medicalMasterRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public MedicalMasterService(MedicalMasterRepository medicalMasterRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.medicalMasterRepository = medicalMasterRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<MedicalMaster> getAllMedicalsForCurrentCompany() {
        return medicalMasterRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
    }

    public MedicalMaster getMedicalByIdForCurrentCompany(Long medicalId) {
        return medicalMasterRepository.findByMedicalIdAndCompanyId(medicalId, companyContext.getCompanyId()).orElse(null);
    }

    public MedicalMaster createMedicalForCurrentCompany(MedicalMaster medical) {
        Long companyId = companyContext.getCompanyId();
        medical.setCompany(companyRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
        medical.setCreatedBy(userIdStr);
        medical.setUpdatedBy(userIdStr);
        return medicalMasterRepository.save(medical);
    }

    public MedicalMaster updateMedicalForCurrentCompany(Long medicalId, MedicalMaster medical) {
        return medicalMasterRepository.findByMedicalIdAndCompanyId(medicalId, companyContext.getCompanyId()).map(existing -> {
            existing.setName(medical.getName());
            existing.setAddress(medical.getAddress());
            existing.setPhoneNumber(medical.getPhoneNumber());
            existing.setEmail(medical.getEmail());
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            return medicalMasterRepository.save(existing);
        }).orElse(null);
    }

    public boolean softDeleteMedicalForCurrentCompany(Long medicalId) {
        return medicalMasterRepository.findByMedicalIdAndCompanyId(medicalId, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            // Set updatedBy from context
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            medicalMasterRepository.save(existing);
            return true;
        }).orElse(false);
    }
}
