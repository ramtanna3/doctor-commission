package com.org.app.dcas.service;

import com.org.app.dcas.model.MrMaster;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.MrMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MrMasterService {

    private static final Logger log = LoggerFactory.getLogger(MrMasterService.class);

    private final MrMasterRepository mrRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public MrMasterService(MrMasterRepository mrRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.mrRepository = mrRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<MrMaster> getAllMrForCurrentCompany() {
        Long companyId = companyContext.getCompanyId();
        log.info("getAllMr - companyId={}", companyId);
        List<MrMaster> result = mrRepository.findByCompanyId(companyId);
        log.info("getAllMr - returned {} MRs", result.size());
        return result;
    }

    public List<MrMaster> getActiveMrForCurrentCompany() {
        Long companyId = companyContext.getCompanyId();
        log.info("getActiveMr - companyId={}", companyId);
        List<MrMaster> result = mrRepository.findByCompanyIdAndIsActiveTrue(companyId);
        log.info("getActiveMr - returned {} active MRs", result.size());
        return result;
    }

    public MrMaster getMrByIdForCurrentCompany(Long mrId) {
        log.info("getMrById - mrId={}", mrId);
        MrMaster result = mrRepository.findByMrIdAndCompanyId(mrId, companyContext.getCompanyId()).orElse(null);
        if (result == null) log.warn("getMrById - mrId={} not found", mrId);
        return result;
    }

    public MrMaster createMrForCurrentCompany(MrMaster mr) {
        Long companyId = companyContext.getCompanyId();
        log.info("createMr - companyId={}, name='{}'", companyId, mr.getName());
        mr.setCompany(companyRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        MrMaster saved = mrRepository.save(mr);
        log.info("createMr - created mrId={}", saved.getMrId());
        return saved;
    }

    public MrMaster updateMrForCurrentCompany(Long mrId, MrMaster mr) {
        log.info("updateMr - mrId={}", mrId);
        return mrRepository.findByMrIdAndCompanyId(mrId, companyContext.getCompanyId()).map(existing -> {
            existing.setName(mr.getName());
            existing.setPhoneNumber(mr.getPhoneNumber());
            existing.setEmail(mr.getEmail());
            MrMaster saved = mrRepository.save(existing);
            log.info("updateMr - mrId={} updated", mrId);
            return saved;
        }).orElseGet(() -> { log.warn("updateMr - mrId={} not found", mrId); return null; });
    }

    public boolean softDeleteMrForCurrentCompany(Long mrId) {
        log.info("softDeleteMr - mrId={}", mrId);
        return mrRepository.findByMrIdAndCompanyId(mrId, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            mrRepository.save(existing);
            log.info("softDeleteMr - mrId={} deactivated", mrId);
            return true;
        }).orElseGet(() -> { log.warn("softDeleteMr - mrId={} not found", mrId); return false; });
    }
}
