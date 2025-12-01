package com.org.app.dcas.service;

import com.org.app.dcas.model.Distributor;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.DistributorRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DistributorService {

    private final DistributorRepository distributorRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public DistributorService(DistributorRepository distributorRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.distributorRepository = distributorRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<Distributor> getAllDistributorsForCurrentCompany() {
        return distributorRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
    }

    public List<Distributor> getActiveDistributorsForCurrentCompany() {
        return distributorRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
    }

    public Distributor getDistributorByIdForCurrentCompany(Long distributorId) {
        return distributorRepository.findByDistributorIdAndCompanyIdAndIsActiveTrue(distributorId, companyContext.getCompanyId()).orElse(null);
    }

    public Distributor createDistributorForCurrentCompany(Distributor distributor) {
        Long companyId = companyContext.getCompanyId();
        distributor.setCompany(companyRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
        distributor.setCreatedBy(userIdStr);
        distributor.setUpdatedBy(userIdStr);
        return distributorRepository.save(distributor);
    }

    public Distributor updateDistributorForCurrentCompany(Long distributorId, Distributor distributor) {
        return distributorRepository.findByDistributorIdAndCompanyIdAndIsActiveTrue(distributorId, companyContext.getCompanyId()).map(existing -> {
            existing.setDistributorName(distributor.getDistributorName());
            existing.setPhoneNumber(distributor.getPhoneNumber());
            existing.setEmail(distributor.getEmail());
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            return distributorRepository.save(existing);
        }).orElse(null);
    }

    public boolean softDeleteDistributorForCurrentCompany(Long distributorId) {
        return distributorRepository.findByDistributorIdAndCompanyIdAndIsActiveTrue(distributorId, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            // Set updatedBy from context
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            distributorRepository.save(existing);
            return true;
        }).orElse(false);
    }
}
