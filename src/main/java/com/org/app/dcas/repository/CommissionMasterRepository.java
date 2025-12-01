package com.org.app.dcas.repository;

import com.org.app.dcas.model.CommissionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CommissionMasterRepository extends JpaRepository<CommissionMaster, Long> {
    List<CommissionMaster> findByCompanyId(Long companyId);
    List<CommissionMaster> findByCompanyIdAndIsActiveTrue(Long companyId);
    Optional<CommissionMaster> findByCommissionIdAndCompanyId(Long commissionId, Long companyId);
}
