package com.org.app.dcas.controller;

import com.org.app.dcas.model.TerritoryMaster;
import com.org.app.dcas.service.TerritoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/territories")
public class TerritoryController {

    private final TerritoryService territoryService;

    public TerritoryController(TerritoryService territoryService) {
        this.territoryService = territoryService;
    }

    @GetMapping
    public List<TerritoryMaster> getAllTerritories() {
        return territoryService.getAllTerritoriesForCurrentCompany();
    }

    @GetMapping("/active")
    public List<TerritoryMaster> getAllActiveTerritories() {
        return territoryService.getActiveTerritoriesForCurrentCompany();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TerritoryMaster> getTerritoryById(@PathVariable Long id) {
        TerritoryMaster territory = territoryService.getTerritoryByIdForCurrentCompany(id);
        return territory != null ? ResponseEntity.ok(territory) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<TerritoryMaster> createTerritory(@RequestBody TerritoryMaster territory) {
        TerritoryMaster saved = territoryService.createTerritoryForCurrentCompany(territory);
        return ResponseEntity.created(URI.create("/api/territories/" + saved.getTerritoryId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TerritoryMaster> updateTerritory(@PathVariable Long id, @RequestBody TerritoryMaster territory) {
        TerritoryMaster updated = territoryService.updateTerritoryForCurrentCompany(id, territory);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteTerritory(@PathVariable Long id) {
        boolean deleted = territoryService.softDeleteTerritoryForCurrentCompany(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}