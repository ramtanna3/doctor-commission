package com.org.app.dcas.repository;

import com.org.app.dcas.model.CommissionMaster;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CommissionMasterRepository extends JpaRepository<CommissionMaster, Long> {
    // EntityGraph joins doctor/medical/product in one query instead of N lazy selects
    @EntityGraph(attributePaths = {"doctor", "medical", "product"})
    List<CommissionMaster> findByCompanyId(Long companyId);

    @EntityGraph(attributePaths = {"doctor", "medical", "product"})
    List<CommissionMaster> findByCompanyIdAndIsActiveTrue(Long companyId);

    Optional<CommissionMaster> findByCommissionIdAndCompanyId(Long commissionId, Long companyId);

    Optional<CommissionMaster> findByDoctorDoctorIdAndMedicalMedicalIdAndProductProductIdAndCompanyId(
            Long doctorId, Long medicalId, Long productId, Long companyId);
}
