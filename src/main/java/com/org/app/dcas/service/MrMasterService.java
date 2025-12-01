package com.org.app.dcas.service;

import com.org.app.dcas.model.MrMaster;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.MrMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MrMasterService {

    private final MrMasterRepository mrRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public MrMasterService(MrMasterRepository mrRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.mrRepository = mrRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<MrMaster> getAllMrForCurrentCompany() {
        return mrRepository.findByCompanyId(companyContext.getCompanyId());
    }

    public List<MrMaster> getActiveMrForCurrentCompany() {
        return mrRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
    }

    public MrMaster getMrByIdForCurrentCompany(Long mrId) {
        return mrRepository.findByMrIdAndCompanyId(mrId, companyContext.getCompanyId()).orElse(null);
    }

    public MrMaster createMrForCurrentCompany(MrMaster mr) {
        Long companyId = companyContext.getCompanyId();
        mr.setCompany(companyRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        return mrRepository.save(mr);
    }

    public MrMaster updateMrForCurrentCompany(Long mrId, MrMaster mr) {
        return mrRepository.findByMrIdAndCompanyId(mrId, companyContext.getCompanyId()).map(existing -> {
            existing.setName(mr.getName());
            existing.setPhoneNumber(mr.getPhoneNumber());
            existing.setEmail(mr.getEmail());
            // ...other fields as needed...
            return mrRepository.save(existing);
        }).orElse(null);
    }

    public boolean softDeleteMrForCurrentCompany(Long mrId) {
        return mrRepository.findByMrIdAndCompanyId(mrId, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            mrRepository.save(existing);
            return true;
        }).orElse(false);
    }
}
