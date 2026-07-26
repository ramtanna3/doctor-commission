package com.org.app.dcas.controller;

import com.org.app.dcas.model.MedicalMaster;
import com.org.app.dcas.service.MedicalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/medicals")
public class MedicalController {

    private static final Logger log = LoggerFactory.getLogger(MedicalController.class);

    private final MedicalService medicalService;

    public MedicalController(MedicalService medicalService) {
        this.medicalService = medicalService;
    }

    @GetMapping
    public List<MedicalMaster> getAllMedicals() {
        log.info("GET /api/medicals - fetching all medicals for current company");
        List<MedicalMaster> result = medicalService.getAllMedicalsForCurrentCompany();
        log.info("GET /api/medicals - returned {} medicals", result.size());
        return result;
    }

    @GetMapping("/active")
    public List<MedicalMaster> getAllActiveMedicals() {
        log.info("GET /api/medicals/active - fetching active medicals");
        List<MedicalMaster> result = medicalService.getActiveMedicalsForCurrentCompany();
        log.info("GET /api/medicals/active - returned {} medicals", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalMaster> getMedicalById(@PathVariable Long id) {
        log.info("GET /api/medicals/{} - fetching medical", id);
        MedicalMaster medical = medicalService.getMedicalByIdForCurrentCompany(id);
        if (medical == null) { log.warn("GET /api/medicals/{} - not found", id); }
        return medical != null ? ResponseEntity.ok(medical) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<MedicalMaster> createMedical(@RequestBody MedicalMaster medical) {
        log.info("POST /api/medicals - creating medical name='{}'", medical.getName());
        MedicalMaster saved = medicalService.createMedicalForCurrentCompany(medical);
        log.info("POST /api/medicals - created medicalId={}", saved.getMedicalId());
        return ResponseEntity.created(URI.create("/api/medicals/" + saved.getMedicalId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalMaster> updateMedical(@PathVariable Long id, @RequestBody MedicalMaster medical) {
        log.info("PUT /api/medicals/{} - updating medical", id);
        MedicalMaster updated = medicalService.updateMedicalForCurrentCompany(id, medical);
        if (updated == null) { log.warn("PUT /api/medicals/{} - not found", id); }
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteMedical(@PathVariable Long id) {
        log.info("DELETE /api/medicals/{} - soft-deleting medical", id);
        boolean deleted = medicalService.softDeleteMedicalForCurrentCompany(id);
        if (!deleted) { log.warn("DELETE /api/medicals/{} - not found", id); }
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}