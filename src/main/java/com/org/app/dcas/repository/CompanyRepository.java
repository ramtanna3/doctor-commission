package com.org.app.dcas.repository;

import com.org.app.dcas.model.Company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCompanyId(Long companyId);
    Optional<Company> findByName(String name);
    List<Company> findByIsActiveTrue();
}
