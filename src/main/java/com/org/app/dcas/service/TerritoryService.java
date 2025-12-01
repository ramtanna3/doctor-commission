package com.org.app.dcas.service;

import com.org.app.dcas.model.TerritoryMaster;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.TerritoryMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TerritoryService {

    private final TerritoryMasterRepository territoryRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public TerritoryService(TerritoryMasterRepository territoryRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.territoryRepository = territoryRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<TerritoryMaster> getAllTerritoriesForCurrentCompany() {
        return territoryRepository.findByCompanyId(companyContext.getCompanyId());
    }

    public List<TerritoryMaster> getActiveTerritoriesForCurrentCompany() {
        return territoryRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
    }

    public TerritoryMaster getTerritoryByIdForCurrentCompany(Long id) {
        return territoryRepository.findByTerritoryIdAndCompanyId(id, companyContext.getCompanyId()).orElse(null);
    }

    public TerritoryMaster createTerritoryForCurrentCompany(TerritoryMaster territory) {
        Long companyId = companyContext.getCompanyId();
        territory.setCompany(companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        return territoryRepository.save(territory);
    }

    public TerritoryMaster updateTerritoryForCurrentCompany(Long id, TerritoryMaster territory) {
        return territoryRepository.findByTerritoryIdAndCompanyId(id, companyContext.getCompanyId()).map(existing -> {
            existing.setName(territory.getName());
            existing.setDescription(territory.getDescription());
            existing.setCity(territory.getCity());
            existing.setState(territory.getState());
            existing.setPincode(territory.getPincode());
            // ...other fields as needed...
            return territoryRepository.save(existing);
        }).orElse(null);
    }

    public boolean softDeleteTerritoryForCurrentCompany(Long id) {
        return territoryRepository.findByTerritoryIdAndCompanyId(id, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            territoryRepository.save(existing);
            return true;
        }).orElse(false);
    }
}
