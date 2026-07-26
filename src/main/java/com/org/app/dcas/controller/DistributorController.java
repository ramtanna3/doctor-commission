package com.org.app.dcas.controller;

import com.org.app.dcas.model.Distributor;
import com.org.app.dcas.service.DistributorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/distributors")
public class DistributorController {

    private static final Logger log = LoggerFactory.getLogger(DistributorController.class);

    private final DistributorService distributorService;

    public DistributorController(DistributorService distributorService) {
        this.distributorService = distributorService;
    }

    @GetMapping
    public List<Distributor> getAllDistributors() {
        log.info("GET /api/distributors - fetching all distributors for current company");
        List<Distributor> result = distributorService.getAllDistributorsForCurrentCompany();
        log.info("GET /api/distributors - returned {} distributors", result.size());
        return result;
    }

    @GetMapping("/active")
    public List<Distributor> getAllActiveDistributors() {
        log.info("GET /api/distributors/active - fetching active distributors");
        List<Distributor> result = distributorService.getActiveDistributorsForCurrentCompany();
        log.info("GET /api/distributors/active - returned {} distributors", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Distributor> getDistributorById(@PathVariable Long id) {
        log.info("GET /api/distributors/{} - fetching distributor", id);
        Distributor distributor = distributorService.getDistributorByIdForCurrentCompany(id);
        if (distributor == null) { log.warn("GET /api/distributors/{} - not found", id); }
        return distributor != null ? ResponseEntity.ok(distributor) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Distributor> createDistributor(@RequestBody Distributor distributor) {
        log.info("POST /api/distributors - creating distributor name='{}'", distributor.getDistributorName());
        Distributor saved = distributorService.createDistributorForCurrentCompany(distributor);
        log.info("POST /api/distributors - created distributorId={}", saved.getDistributorId());
        return ResponseEntity.created(URI.create("/api/distributors/" + saved.getDistributorId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Distributor> updateDistributor(@PathVariable Long id, @RequestBody Distributor distributor) {
        log.info("PUT /api/distributors/{} - updating distributor", id);
        Distributor updated = distributorService.updateDistributorForCurrentCompany(id, distributor);
        if (updated == null) { log.warn("PUT /api/distributors/{} - not found", id); }
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteDistributor(@PathVariable Long id) {
        log.info("DELETE /api/distributors/{} - soft-deleting distributor", id);
        boolean deleted = distributorService.softDeleteDistributorForCurrentCompany(id);
        if (!deleted) { log.warn("DELETE /api/distributors/{} - not found", id); }
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
