package com.org.app.dcas.repository;

import com.org.app.dcas.model.TerritoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TerritoryMasterRepository extends JpaRepository<TerritoryMaster, Long> {
    List<TerritoryMaster> findByCompanyId(Long companyId);
    List<TerritoryMaster> findByCompanyIdAndIsActiveTrue(Long companyId);
    Optional<TerritoryMaster> findByTerritoryIdAndCompanyId(Long territoryId, Long companyId);
}
