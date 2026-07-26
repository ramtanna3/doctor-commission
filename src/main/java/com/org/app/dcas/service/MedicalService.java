package com.org.app.dcas.service;

import com.org.app.dcas.model.MedicalMaster;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.MedicalMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicalService {

    private static final Logger log = LoggerFactory.getLogger(MedicalService.class);

    private final MedicalMasterRepository medicalRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public MedicalService(MedicalMasterRepository medicalRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.medicalRepository = medicalRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<MedicalMaster> getAllMedicalsForCurrentCompany() {
        Long companyId = companyContext.getCompanyId();
        log.info("getAllMedicals - companyId={}", companyId);
        List<MedicalMaster> result = medicalRepository.findByCompanyIdAndIsActiveTrue(companyId);
        log.info("getAllMedicals - returned {} medicals", result.size());
        return result;
    }

    public List<MedicalMaster> getActiveMedicalsForCurrentCompany() {
        return getAllMedicalsForCurrentCompany();
    }

    public MedicalMaster getMedicalByIdForCurrentCompany(Long id) {
        log.info("getMedicalById - medicalId={}", id);
        MedicalMaster result = medicalRepository.findByMedicalIdAndCompanyId(id, companyContext.getCompanyId()).orElse(null);
        if (result == null) log.warn("getMedicalById - medicalId={} not found", id);
        return result;
    }

    public MedicalMaster createMedicalForCurrentCompany(MedicalMaster medical) {
        Long companyId = companyContext.getCompanyId();
        log.info("createMedical - companyId={}, name='{}'", companyId, medical.getName());
        medical.setCompany(companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        MedicalMaster saved = medicalRepository.save(medical);
        log.info("createMedical - created medicalId={}", saved.getMedicalId());
        return saved;
    }

    public MedicalMaster updateMedicalForCurrentCompany(Long id, MedicalMaster medical) {
        log.info("updateMedical - medicalId={}", id);
        return medicalRepository.findByMedicalIdAndCompanyId(id, companyContext.getCompanyId()).map(existing -> {
            existing.setName(medical.getName());
            existing.setAddress(medical.getAddress());
            existing.setPhoneNumber(medical.getPhoneNumber());
            existing.setEmail(medical.getEmail());
            existing.setTerritory(medical.getTerritory());
            MedicalMaster saved = medicalRepository.save(existing);
            log.info("updateMedical - medicalId={} updated", id);
            return saved;
        }).orElseGet(() -> { log.warn("updateMedical - medicalId={} not found", id); return null; });
    }

    public boolean softDeleteMedicalForCurrentCompany(Long id) {
        log.info("softDeleteMedical - medicalId={}", id);
        return medicalRepository.findByMedicalIdAndCompanyId(id, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            medicalRepository.save(existing);
            log.info("softDeleteMedical - medicalId={} deactivated", id);
            return true;
        }).orElseGet(() -> { log.warn("softDeleteMedical - medicalId={} not found", id); return false; });
    }
}
