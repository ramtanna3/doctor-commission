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
public class MedicalMasterService {

    private static final Logger log = LoggerFactory.getLogger(MedicalMasterService.class);

    private final MedicalMasterRepository medicalMasterRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public MedicalMasterService(MedicalMasterRepository medicalMasterRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.medicalMasterRepository = medicalMasterRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<MedicalMaster> getAllMedicalsForCurrentCompany() {
        Long companyId = companyContext.getCompanyId();
        log.info("getAllMedicals - companyId={}", companyId);
        List<MedicalMaster> result = medicalMasterRepository.findByCompanyIdAndIsActiveTrue(companyId);
        log.info("getAllMedicals - returned {} medicals", result.size());
        return result;
    }

    public MedicalMaster getMedicalByIdForCurrentCompany(Long medicalId) {
        log.info("getMedicalById - medicalId={}", medicalId);
        MedicalMaster result = medicalMasterRepository.findByMedicalIdAndCompanyId(medicalId, companyContext.getCompanyId()).orElse(null);
        if (result == null) log.warn("getMedicalById - medicalId={} not found", medicalId);
        return result;
    }

    public MedicalMaster createMedicalForCurrentCompany(MedicalMaster medical) {
        Long companyId = companyContext.getCompanyId();
        log.info("createMedical - companyId={}, name='{}'", companyId, medical.getName());
        medical.setCompany(companyRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
        medical.setCreatedBy(userIdStr);
        medical.setUpdatedBy(userIdStr);
        MedicalMaster saved = medicalMasterRepository.save(medical);
        log.info("createMedical - created medicalId={}", saved.getMedicalId());
        return saved;
    }

    public MedicalMaster updateMedicalForCurrentCompany(Long medicalId, MedicalMaster medical) {
        log.info("updateMedical - medicalId={}", medicalId);
        return medicalMasterRepository.findByMedicalIdAndCompanyId(medicalId, companyContext.getCompanyId()).map(existing -> {
            existing.setName(medical.getName());
            existing.setAddress(medical.getAddress());
            existing.setPhoneNumber(medical.getPhoneNumber());
            existing.setEmail(medical.getEmail());
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            MedicalMaster saved = medicalMasterRepository.save(existing);
            log.info("updateMedical - medicalId={} updated", medicalId);
            return saved;
        }).orElseGet(() -> { log.warn("updateMedical - medicalId={} not found", medicalId); return null; });
    }

    public boolean softDeleteMedicalForCurrentCompany(Long medicalId) {
        log.info("softDeleteMedical - medicalId={}", medicalId);
        return medicalMasterRepository.findByMedicalIdAndCompanyId(medicalId, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            medicalMasterRepository.save(existing);
            log.info("softDeleteMedical - medicalId={} deactivated", medicalId);
            return true;
        }).orElseGet(() -> { log.warn("softDeleteMedical - medicalId={} not found", medicalId); return false; });
    }
}
