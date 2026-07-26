package com.org.app.dcas.service;

import com.org.app.dcas.model.DoctorMaster;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.DoctorMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);

    private final DoctorMasterRepository doctorMasterRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public DoctorService(DoctorMasterRepository doctorMasterRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.doctorMasterRepository = doctorMasterRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<DoctorMaster> getAllDoctorsForCurrentCompany() {
        Long companyId = companyContext.getCompanyId();
        log.info("getAllDoctors - companyId={}", companyId);
        List<DoctorMaster> result = doctorMasterRepository.findByCompanyIdAndIsActiveTrue(companyId);
        log.info("getAllDoctors - returned {} doctors", result.size());
        return result;
    }

    public List<DoctorMaster> getActiveDoctorsForCurrentCompany() {
        return getAllDoctorsForCurrentCompany();
    }

    public DoctorMaster getDoctorByIdForCurrentCompany(Long doctorId) {
        log.info("getDoctorById - doctorId={}", doctorId);
        DoctorMaster result = doctorMasterRepository.findByDoctorIdAndCompanyId(doctorId, companyContext.getCompanyId()).orElse(null);
        if (result == null) log.warn("getDoctorById - doctorId={} not found", doctorId);
        return result;
    }

    public DoctorMaster createDoctorForCurrentCompany(DoctorMaster doctor) {
        Long companyId = companyContext.getCompanyId();
        log.info("createDoctor - companyId={}, name='{}'", companyId, doctor.getName());
        doctor.setCompany(companyRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
        doctor.setCreatedBy(userIdStr);
        doctor.setUpdatedBy(userIdStr);
        DoctorMaster saved = doctorMasterRepository.save(doctor);
        log.info("createDoctor - created doctorId={}", saved.getDoctorId());
        return saved;
    }

    public DoctorMaster updateDoctorForCurrentCompany(Long doctorId, DoctorMaster doctor) {
        log.info("updateDoctor - doctorId={}", doctorId);
        return doctorMasterRepository.findByDoctorIdAndCompanyId(doctorId, companyContext.getCompanyId()).map(existing -> {
            existing.setName(doctor.getName());
            existing.setSpecialization(doctor.getSpecialization());
            existing.setPhoneNumber(doctor.getPhoneNumber());
            existing.setEmail(doctor.getEmail());
            existing.setMr(doctor.getMr());
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            DoctorMaster saved = doctorMasterRepository.save(existing);
            log.info("updateDoctor - doctorId={} updated", doctorId);
            return saved;
        }).orElseGet(() -> { log.warn("updateDoctor - doctorId={} not found", doctorId); return null; });
    }

    public boolean softDeleteDoctorForCurrentCompany(Long doctorId) {
        log.info("softDeleteDoctor - doctorId={}", doctorId);
        return doctorMasterRepository.findByDoctorIdAndCompanyId(doctorId, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            doctorMasterRepository.save(existing);
            log.info("softDeleteDoctor - doctorId={} deactivated", doctorId);
            return true;
        }).orElseGet(() -> { log.warn("softDeleteDoctor - doctorId={} not found", doctorId); return false; });
    }
}

