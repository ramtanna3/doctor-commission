package com.org.app.dcas.repository;

import com.org.app.dcas.model.DoctorMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DoctorMasterRepository extends JpaRepository<DoctorMaster, Long> {
    List<DoctorMaster> findByCompanyId(Long companyId);
    List<DoctorMaster> findByCompanyIdAndIsActiveTrue(Long companyId);
    Optional<DoctorMaster> findByDoctorIdAndCompanyId(Long doctorId, Long companyId);
    Optional<DoctorMaster> findByNameIgnoreCaseAndCompanyIdAndIsActiveTrue(String name, Long companyId);
}
