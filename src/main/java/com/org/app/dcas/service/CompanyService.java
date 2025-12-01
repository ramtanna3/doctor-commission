package com.org.app.dcas.service;

import com.org.app.dcas.model.Company;
import com.org.app.dcas.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public List<Company> getActiveCompanies() {
        return companyRepository.findByIsActiveTrue();
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id).orElse(null);
    }

    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    public Company updateCompany(Long id, Company company) {
        return companyRepository.findById(id).map(existing -> {
            existing.setName(company.getName());
            existing.setAddress(company.getAddress());
            existing.setPhoneNumber(company.getPhoneNumber());
            existing.setEmail(company.getEmail());
            // ...other fields as needed...
            return companyRepository.save(existing);
        }).orElse(null);
    }

    public boolean softDeleteCompany(Long id) {
        return companyRepository.findById(id).map(existing -> {
            existing.setIsActive(false);
            companyRepository.save(existing);
            return true;
        }).orElse(false);
    }
}
