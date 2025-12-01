package com.org.app.dcas.repository;

import com.org.app.dcas.model.ProductMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductMasterRepository extends JpaRepository<ProductMaster, Long> {
    List<ProductMaster> findByCompanyIdAndIsActiveTrue(Long companyId);
    Optional<ProductMaster> findByProductIdAndCompanyId(Long productId, Long companyId);
    List<ProductMaster> findByNameIgnoreCaseAndCompanyIdAndIsActiveTrue(String name, Long companyId);
}
