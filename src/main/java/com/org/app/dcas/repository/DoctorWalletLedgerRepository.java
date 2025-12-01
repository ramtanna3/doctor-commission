package com.org.app.dcas.repository;

import com.org.app.dcas.model.DoctorWalletLedger;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DoctorWalletLedgerRepository extends JpaRepository<DoctorWalletLedger, Long> {
    List<DoctorWalletLedger> findByDoctorId(Long doctorId);

    boolean existsByReferenceIdAndReferenceType(Long referenceId, DoctorWalletLedger.ReferenceType referenceType);

    @Query("SELECT SUM(d.creditAmount) - SUM(d.debitAmount) FROM DoctorWalletLedger d WHERE d.doctor.id = :doctorId")
    Double getWalletBalanceByDoctorId(Long doctorId);
}
