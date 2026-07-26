package com.org.app.dcas.controller;

import com.org.app.dcas.model.TerritoryMaster;
import com.org.app.dcas.service.TerritoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/territories")
public class TerritoryController {

    private static final Logger log = LoggerFactory.getLogger(TerritoryController.class);

    private final TerritoryService territoryService;

    public TerritoryController(TerritoryService territoryService) {
        this.territoryService = territoryService;
    }

    @GetMapping
    public List<TerritoryMaster> getAllTerritories() {
        log.info("GET /api/territories - fetching all territories for current company");
        List<TerritoryMaster> result = territoryService.getAllTerritoriesForCurrentCompany();
        log.info("GET /api/territories - returned {} territories", result.size());
        return result;
    }

    @GetMapping("/active")
    public List<TerritoryMaster> getAllActiveTerritories() {
        log.info("GET /api/territories/active - fetching active territories");
        List<TerritoryMaster> result = territoryService.getActiveTerritoriesForCurrentCompany();
        log.info("GET /api/territories/active - returned {} territories", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TerritoryMaster> getTerritoryById(@PathVariable Long id) {
        log.info("GET /api/territories/{} - fetching territory", id);
        TerritoryMaster territory = territoryService.getTerritoryByIdForCurrentCompany(id);
        if (territory == null) { log.warn("GET /api/territories/{} - not found", id); }
        return territory != null ? ResponseEntity.ok(territory) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<TerritoryMaster> createTerritory(@RequestBody TerritoryMaster territory) {
        log.info("POST /api/territories - creating territory name='{}'", territory.getName());
        TerritoryMaster saved = territoryService.createTerritoryForCurrentCompany(territory);
        log.info("POST /api/territories - created territoryId={}", saved.getTerritoryId());
        return ResponseEntity.created(URI.create("/api/territories/" + saved.getTerritoryId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TerritoryMaster> updateTerritory(@PathVariable Long id, @RequestBody TerritoryMaster territory) {
        log.info("PUT /api/territories/{} - updating territory", id);
        TerritoryMaster updated = territoryService.updateTerritoryForCurrentCompany(id, territory);
        if (updated == null) { log.warn("PUT /api/territories/{} - not found", id); }
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteTerritory(@PathVariable Long id) {
        log.info("DELETE /api/territories/{} - soft-deleting territory", id);
        boolean deleted = territoryService.softDeleteTerritoryForCurrentCompany(id);
        if (!deleted) { log.warn("DELETE /api/territories/{} - not found", id); }
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}