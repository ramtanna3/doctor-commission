package com.org.app.dcas.repository;

import com.org.app.dcas.model.DoctorWalletLedger;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorWalletLedgerRepository extends JpaRepository<DoctorWalletLedger, Long> {
    // Single JOIN query covers:
    //  - doctor + its full nested chain (DoctorWalletLedgerListResponse wraps full DoctorMaster)
    //  - salesTransaction.product (used in DoctorWalletLedgerResponse.from() for productName)
    @EntityGraph(attributePaths = {
        "doctor", "doctor.company",
        "doctor.mr", "doctor.mr.company", "doctor.mr.territory", "doctor.mr.territory.company",
        "salesTransaction", "salesTransaction.product"
    })
    List<DoctorWalletLedger> findByDoctorId(Long doctorId);

    boolean existsByReferenceIdAndReferenceType(Long referenceId, DoctorWalletLedger.ReferenceType referenceType);

    @Query("SELECT SUM(d.creditAmount) - SUM(d.debitAmount) FROM DoctorWalletLedger d WHERE d.doctor.id = :doctorId")
    Double getWalletBalanceByDoctorId(Long doctorId);

    /**
     * Returns [doctor_id, balance] rows for all given doctors in a single query.
     * Replaces the N per-doctor queries in getAllDoctorBalancesForCompany.
     */
    @Query(value = "SELECT dwl.doctor_id, " +
           "COALESCE(SUM(dwl.credit_amount), 0.0) - COALESCE(SUM(dwl.debit_amount), 0.0) AS balance " +
           "FROM doctor_wallet_ledger dwl " +
           "WHERE dwl.doctor_id IN :doctorIds " +
           "GROUP BY dwl.doctor_id", nativeQuery = true)
    List<Object[]> findBalancesByDoctorIds(@Param("doctorIds") List<Long> doctorIds);
}
