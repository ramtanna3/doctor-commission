package com.org.app.dcas.controller;

import com.org.app.dcas.model.MrMaster;
import com.org.app.dcas.service.MrMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/mrs")
public class MrMasterController {

    private static final Logger log = LoggerFactory.getLogger(MrMasterController.class);

    private final MrMasterService mrService;

    public MrMasterController(MrMasterService mrService) {
        this.mrService = mrService;
    }

    @GetMapping
    public List<MrMaster> getAllMrs() {
        log.info("GET /api/mrs - fetching all MRs for current company");
        List<MrMaster> result = mrService.getAllMrForCurrentCompany();
        log.info("GET /api/mrs - returned {} MRs", result.size());
        return result;
    }

    @GetMapping("/active")
    public List<MrMaster> getAllActiveMrs() {
        log.info("GET /api/mrs/active - fetching active MRs");
        List<MrMaster> result = mrService.getActiveMrForCurrentCompany();
        log.info("GET /api/mrs/active - returned {} MRs", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MrMaster> getMrById(@PathVariable Long id) {
        log.info("GET /api/mrs/{} - fetching MR", id);
        MrMaster mr = mrService.getMrByIdForCurrentCompany(id);
        if (mr == null) { log.warn("GET /api/mrs/{} - not found", id); }
        return mr != null ? ResponseEntity.ok(mr) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<MrMaster> createMr(@RequestBody MrMaster mr) {
        log.info("POST /api/mrs - creating MR name='{}'", mr.getName());
        MrMaster saved = mrService.createMrForCurrentCompany(mr);
        log.info("POST /api/mrs - created mrId={}", saved.getMrId());
        return ResponseEntity.created(URI.create("/api/mrs/" + saved.getMrId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MrMaster> updateMr(@PathVariable Long id, @RequestBody MrMaster mr) {
        log.info("PUT /api/mrs/{} - updating MR", id);
        MrMaster updated = mrService.updateMrForCurrentCompany(id, mr);
        if (updated == null) { log.warn("PUT /api/mrs/{} - not found", id); }
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteMr(@PathVariable Long id) {
        log.info("DELETE /api/mrs/{} - soft-deleting MR", id);
        boolean deleted = mrService.softDeleteMrForCurrentCompany(id);
        if (!deleted) { log.warn("DELETE /api/mrs/{} - not found", id); }
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}