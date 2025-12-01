package com.org.app.dcas.repository;

import com.org.app.dcas.model.MedicalMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MedicalMasterRepository extends JpaRepository<MedicalMaster, Long> {
    List<MedicalMaster> findByCompanyIdAndIsActiveTrue(Long companyId);
    Optional<MedicalMaster> findByMedicalIdAndCompanyId(Long medicalId, Long companyId);
    List<MedicalMaster> findByNameIgnoreCaseAndCompanyIdAndIsActiveTrue(String name, Long companyId);
}
