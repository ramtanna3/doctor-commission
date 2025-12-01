package com.org.app.dcas.controller;

import com.org.app.dcas.model.Distributor;
import com.org.app.dcas.service.DistributorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/distributors")
public class DistributorController {

    private final DistributorService distributorService;

    public DistributorController(DistributorService distributorService) {
        this.distributorService = distributorService;
    }

    @GetMapping
    public List<Distributor> getAllDistributors() {
        return distributorService.getAllDistributorsForCurrentCompany();
    }

    @GetMapping("/active")
    public List<Distributor> getAllActiveDistributors() {
        return distributorService.getActiveDistributorsForCurrentCompany();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Distributor> getDistributorById(@PathVariable Long id) {
        Distributor distributor = distributorService.getDistributorByIdForCurrentCompany(id);
        return distributor != null ? ResponseEntity.ok(distributor) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Distributor> createDistributor(@RequestBody Distributor distributor) {
        Distributor saved = distributorService.createDistributorForCurrentCompany(distributor);
        return ResponseEntity.created(URI.create("/api/distributors/" + saved.getDistributorId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Distributor> updateDistributor(@PathVariable Long id, @RequestBody Distributor distributor) {
        Distributor updated = distributorService.updateDistributorForCurrentCompany(id, distributor);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteDistributor(@PathVariable Long id) {
        boolean deleted = distributorService.softDeleteDistributorForCurrentCompany(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
