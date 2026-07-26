package com.org.app.dcas.repository;

import com.org.app.dcas.model.Distributor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import java.util.List;
import java.util.Optional;

public interface DistributorRepository extends JpaRepository<Distributor, Long> {
    // Eagerly load company so SalesTransactionListResponse.distributor serialises without an extra query
    @EntityGraph(attributePaths = {"company"})
    @Override
    @NonNull Optional<Distributor> findById(@NonNull Long id);

    List<Distributor> findByCompanyIdAndIsActiveTrue(Long companyId);
    Optional<Distributor> findByDistributorIdAndCompanyId(Long distributorId, Long companyId);
    Optional<Distributor> findByDistributorNameAndCompanyId(String distributorName, Long companyId);
    Optional<Distributor> findByDistributorIdAndCompanyIdAndIsActiveTrue(Long distributorId, Long companyId);
}
