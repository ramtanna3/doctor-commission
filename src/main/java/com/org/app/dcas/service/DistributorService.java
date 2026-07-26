package com.org.app.dcas.service;

import com.org.app.dcas.model.Distributor;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.DistributorRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DistributorService {

    private static final Logger log = LoggerFactory.getLogger(DistributorService.class);

    private final DistributorRepository distributorRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public DistributorService(DistributorRepository distributorRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.distributorRepository = distributorRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<Distributor> getAllDistributorsForCurrentCompany() {
        Long companyId = companyContext.getCompanyId();
        log.info("getAllDistributors - companyId={}", companyId);
        List<Distributor> result = distributorRepository.findByCompanyIdAndIsActiveTrue(companyId);
        log.info("getAllDistributors - returned {} distributors", result.size());
        return result;
    }

    public List<Distributor> getActiveDistributorsForCurrentCompany() {
        return getAllDistributorsForCurrentCompany();
    }

    public Distributor getDistributorByIdForCurrentCompany(Long distributorId) {
        log.info("getDistributorById - distributorId={}", distributorId);
        Distributor result = distributorRepository.findByDistributorIdAndCompanyIdAndIsActiveTrue(distributorId, companyContext.getCompanyId()).orElse(null);
        if (result == null) log.warn("getDistributorById - distributorId={} not found", distributorId);
        return result;
    }

    public Distributor createDistributorForCurrentCompany(Distributor distributor) {
        Long companyId = companyContext.getCompanyId();
        log.info("createDistributor - companyId={}, name='{}'", companyId, distributor.getDistributorName());
        distributor.setCompany(companyRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
        distributor.setCreatedBy(userIdStr);
        distributor.setUpdatedBy(userIdStr);
        Distributor saved = distributorRepository.save(distributor);
        log.info("createDistributor - created distributorId={}", saved.getDistributorId());
        return saved;
    }

    public Distributor updateDistributorForCurrentCompany(Long distributorId, Distributor distributor) {
        log.info("updateDistributor - distributorId={}", distributorId);
        return distributorRepository.findByDistributorIdAndCompanyIdAndIsActiveTrue(distributorId, companyContext.getCompanyId()).map(existing -> {
            existing.setDistributorName(distributor.getDistributorName());
            existing.setPhoneNumber(distributor.getPhoneNumber());
            existing.setEmail(distributor.getEmail());
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            Distributor saved = distributorRepository.save(existing);
            log.info("updateDistributor - distributorId={} updated", distributorId);
            return saved;
        }).orElseGet(() -> { log.warn("updateDistributor - distributorId={} not found", distributorId); return null; });
    }

    public boolean softDeleteDistributorForCurrentCompany(Long distributorId) {
        log.info("softDeleteDistributor - distributorId={}", distributorId);
        return distributorRepository.findByDistributorIdAndCompanyIdAndIsActiveTrue(distributorId, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            distributorRepository.save(existing);
            log.info("softDeleteDistributor - distributorId={} deactivated", distributorId);
            return true;
        }).orElseGet(() -> { log.warn("softDeleteDistributor - distributorId={} not found", distributorId); return false; });
    }
}
