package com.org.app.dcas.controller;

import com.org.app.dcas.model.Company;
import com.org.app.dcas.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public List<Company> getAllCompanies() {
        log.info("GET /api/companies - fetching all companies");
        List<Company> result = companyService.getAllCompanies();
        log.info("GET /api/companies - returned {} companies", result.size());
        return result;
    }

    @GetMapping("/active")
    public List<Company> getAllActiveCompanies() {
        log.info("GET /api/companies/active - fetching active companies");
        List<Company> result = companyService.getActiveCompanies();
        log.info("GET /api/companies/active - returned {} companies", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        log.info("GET /api/companies/{} - fetching company", id);
        Company company = companyService.getCompanyById(id);
        if (company == null) { log.warn("GET /api/companies/{} - not found", id); }
        return company != null ? ResponseEntity.ok(company) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        log.info("POST /api/companies - creating company name='{}'", company.getName());
        Company saved = companyService.createCompany(company);
        log.info("POST /api/companies - created companyId={}", saved.getCompanyId());
        return ResponseEntity.created(URI.create("/api/companies/" + saved.getCompanyId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        log.info("PUT /api/companies/{} - updating company", id);
        Company updated = companyService.updateCompany(id, company);
        if (updated == null) { log.warn("PUT /api/companies/{} - not found", id); }
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteCompany(@PathVariable Long id) {
        log.info("DELETE /api/companies/{} - soft-deleting company", id);
        boolean deleted = companyService.softDeleteCompany(id);
        if (!deleted) { log.warn("DELETE /api/companies/{} - not found", id); }
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
