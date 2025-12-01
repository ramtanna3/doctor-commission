package com.org.app.dcas.repository;

import com.org.app.dcas.model.MrMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MrMasterRepository extends JpaRepository<MrMaster, Long> {
    List<MrMaster> findByCompanyId(Long companyId);
    List<MrMaster> findByCompanyIdAndIsActiveTrue(Long companyId);
    Optional<MrMaster> findByMrIdAndCompanyId(Long mrId, Long companyId);
}
