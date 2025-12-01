package com.org.app.dcas.service;

import com.org.app.dcas.model.MedicalMaster;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.MedicalMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicalService {

    private final MedicalMasterRepository medicalRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public MedicalService(MedicalMasterRepository medicalRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.medicalRepository = medicalRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<MedicalMaster> getAllMedicalsForCurrentCompany() {
        return medicalRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
    }

    public List<MedicalMaster> getActiveMedicalsForCurrentCompany() {
        return medicalRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
    }

    public MedicalMaster getMedicalByIdForCurrentCompany(Long id) {
        return medicalRepository.findByMedicalIdAndCompanyId(id, companyContext.getCompanyId()).orElse(null);
    }

    public MedicalMaster createMedicalForCurrentCompany(MedicalMaster medical) {
        Long companyId = companyContext.getCompanyId();
        medical.setCompany(companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        return medicalRepository.save(medical);
    }

    public MedicalMaster updateMedicalForCurrentCompany(Long id, MedicalMaster medical) {
        return medicalRepository.findByMedicalIdAndCompanyId(id, companyContext.getCompanyId()).map(existing -> {
            existing.setName(medical.getName());
            existing.setAddress(medical.getAddress());
            existing.setPhoneNumber(medical.getPhoneNumber());
            existing.setEmail(medical.getEmail());
            existing.setTerritory(medical.getTerritory());
            // ...other fields as needed...
            return medicalRepository.save(existing);
        }).orElse(null);
    }

    public boolean softDeleteMedicalForCurrentCompany(Long id) {
        return medicalRepository.findByMedicalIdAndCompanyId(id, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            medicalRepository.save(existing);
            return true;
        }).orElse(false);
    }
}
