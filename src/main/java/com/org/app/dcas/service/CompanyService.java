package com.org.app.dcas.service;

import com.org.app.dcas.model.Company;
import com.org.app.dcas.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CompanyService {

    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getAllCompanies() {
        log.info("getAllCompanies");
        List<Company> result = companyRepository.findAll();
        log.info("getAllCompanies - returned {} companies", result.size());
        return result;
    }

    public List<Company> getActiveCompanies() {
        log.info("getActiveCompanies");
        List<Company> result = companyRepository.findByIsActiveTrue();
        log.info("getActiveCompanies - returned {} active companies", result.size());
        return result;
    }

    public Company getCompanyById(Long id) {
        log.info("getCompanyById - companyId={}", id);
        Company result = companyRepository.findById(id).orElse(null);
        if (result == null) log.warn("getCompanyById - companyId={} not found", id);
        return result;
    }

    public Company createCompany(Company company) {
        log.info("createCompany - name='{}'", company.getName());
        Company saved = companyRepository.save(company);
        log.info("createCompany - created companyId={}", saved.getCompanyId());
        return saved;
    }

    public Company updateCompany(Long id, Company company) {
        log.info("updateCompany - companyId={}", id);
        return companyRepository.findById(id).map(existing -> {
            existing.setName(company.getName());
            existing.setAddress(company.getAddress());
            existing.setPhoneNumber(company.getPhoneNumber());
            existing.setEmail(company.getEmail());
            Company saved = companyRepository.save(existing);
            log.info("updateCompany - companyId={} updated", id);
            return saved;
        }).orElseGet(() -> { log.warn("updateCompany - companyId={} not found", id); return null; });
    }

    public boolean softDeleteCompany(Long id) {
        log.info("softDeleteCompany - companyId={}", id);
        return companyRepository.findById(id).map(existing -> {
            existing.setIsActive(false);
            companyRepository.save(existing);
            log.info("softDeleteCompany - companyId={} deactivated", id);
            return true;
        }).orElseGet(() -> { log.warn("softDeleteCompany - companyId={} not found", id); return false; });
    }
}
