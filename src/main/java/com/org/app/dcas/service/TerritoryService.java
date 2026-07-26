package com.org.app.dcas.service;

import com.org.app.dcas.model.TerritoryMaster;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.TerritoryMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TerritoryService {

    private static final Logger log = LoggerFactory.getLogger(TerritoryService.class);

    private final TerritoryMasterRepository territoryRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public TerritoryService(TerritoryMasterRepository territoryRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.territoryRepository = territoryRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<TerritoryMaster> getAllTerritoriesForCurrentCompany() {
        Long companyId = companyContext.getCompanyId();
        log.info("getAllTerritories - companyId={}", companyId);
        List<TerritoryMaster> result = territoryRepository.findByCompanyId(companyId);
        log.info("getAllTerritories - returned {} territories", result.size());
        return result;
    }

    public List<TerritoryMaster> getActiveTerritoriesForCurrentCompany() {
        Long companyId = companyContext.getCompanyId();
        log.info("getActiveTerritories - companyId={}", companyId);
        List<TerritoryMaster> result = territoryRepository.findByCompanyIdAndIsActiveTrue(companyId);
        log.info("getActiveTerritories - returned {} active territories", result.size());
        return result;
    }

    public TerritoryMaster getTerritoryByIdForCurrentCompany(Long id) {
        log.info("getTerritoryById - territoryId={}", id);
        TerritoryMaster result = territoryRepository.findByTerritoryIdAndCompanyId(id, companyContext.getCompanyId()).orElse(null);
        if (result == null) log.warn("getTerritoryById - territoryId={} not found", id);
        return result;
    }

    public TerritoryMaster createTerritoryForCurrentCompany(TerritoryMaster territory) {
        Long companyId = companyContext.getCompanyId();
        log.info("createTerritory - companyId={}, name='{}'", companyId, territory.getName());
        territory.setCompany(companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        TerritoryMaster saved = territoryRepository.save(territory);
        log.info("createTerritory - created territoryId={}", saved.getTerritoryId());
        return saved;
    }

    public TerritoryMaster updateTerritoryForCurrentCompany(Long id, TerritoryMaster territory) {
        log.info("updateTerritory - territoryId={}", id);
        return territoryRepository.findByTerritoryIdAndCompanyId(id, companyContext.getCompanyId()).map(existing -> {
            existing.setName(territory.getName());
            existing.setDescription(territory.getDescription());
            existing.setCity(territory.getCity());
            existing.setState(territory.getState());
            existing.setPincode(territory.getPincode());
            TerritoryMaster saved = territoryRepository.save(existing);
            log.info("updateTerritory - territoryId={} updated", id);
            return saved;
        }).orElseGet(() -> { log.warn("updateTerritory - territoryId={} not found", id); return null; });
    }

    public boolean softDeleteTerritoryForCurrentCompany(Long id) {
        log.info("softDeleteTerritory - territoryId={}", id);
        return territoryRepository.findByTerritoryIdAndCompanyId(id, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            territoryRepository.save(existing);
            log.info("softDeleteTerritory - territoryId={} deactivated", id);
            return true;
        }).orElseGet(() -> { log.warn("softDeleteTerritory - territoryId={} not found", id); return false; });
    }
}
