package com.org.app.dcas.repository;

import com.org.app.dcas.model.SalesTransaction;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SalesTransactionRepository extends JpaRepository<SalesTransaction, Long> {
    boolean existsByCompanyIdAndMedicalIdAndProductIdAndVoucherNoAndFileAuditIdNot(
        Long companyId, Long medicalId, Long productId, String voucherNo, Long fileAuditId);

    boolean existsByCompanyIdAndRawMedicalNameAndRawProductNameAndVoucherNoAndFileAuditIdNot(
        Long companyId, String rawMedicalName, String rawProductName, String voucherNo, Long fileAuditId);

    @Query("SELECT DISTINCT tx.doctor.doctorId FROM SalesTransaction tx WHERE tx.company.companyId = :companyId AND tx.doctor IS NOT NULL")
    Set<Long> findDistinctDoctorIdsByCompanyId(Long companyId);

    @Query("SELECT tx FROM SalesTransaction tx WHERE tx.doctor.doctorId = :doctorId AND tx.company.companyId = :companyId")
    List<SalesTransaction> findByDoctorIdAndCompanyId(Long doctorId, Long companyId);

    // Single JOIN query covers all associations accessed by SalesTransactionResponse.from()
    // medical.territory.company: MedicalResponse.territory is serialised as full entity
    // doctor.mr.*: DoctorResponse.mr is serialised as full entity including its company/territory
    @EntityGraph(attributePaths = {
        "distributor",
        "medical", "medical.territory", "medical.territory.company",
        "product",
        "doctor", "doctor.mr", "doctor.mr.company", "doctor.mr.territory", "doctor.mr.territory.company",
        "fileAudit"
    })
    List<SalesTransaction> findByDistributorDistributorId(Long distributorId);

    @EntityGraph(attributePaths = {
        "distributor",
        "medical", "medical.territory", "medical.territory.company",
        "product",
        "doctor", "doctor.mr", "doctor.mr.company", "doctor.mr.territory", "doctor.mr.territory.company",
        "fileAudit"
    })
    List<SalesTransaction> findByDistributorDistributorIdAndIsMatchedTrue(Long distributorId);

    @EntityGraph(attributePaths = {
        "distributor",
        "medical", "medical.territory", "medical.territory.company",
        "product",
        "doctor", "doctor.mr", "doctor.mr.company", "doctor.mr.territory", "doctor.mr.territory.company",
        "fileAudit"
    })
    List<SalesTransaction> findByDistributorDistributorIdAndIsMatchedFalse(Long distributorId);
}

