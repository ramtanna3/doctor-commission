package com.org.app.dcas.controller;

import com.org.app.dcas.model.MedicalMaster;
import com.org.app.dcas.service.MedicalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/medicals")
public class MedicalController {

    private final MedicalService medicalService;

    public MedicalController(MedicalService medicalService) {
        this.medicalService = medicalService;
    }

    @GetMapping
    public List<MedicalMaster> getAllMedicals() {
        return medicalService.getAllMedicalsForCurrentCompany();
    }

    @GetMapping("/active")
    public List<MedicalMaster> getAllActiveMedicals() {
        return medicalService.getActiveMedicalsForCurrentCompany();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalMaster> getMedicalById(@PathVariable Long id) {
        MedicalMaster medical = medicalService.getMedicalByIdForCurrentCompany(id);
        return medical != null ? ResponseEntity.ok(medical) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<MedicalMaster> createMedical(@RequestBody MedicalMaster medical) {
        MedicalMaster saved = medicalService.createMedicalForCurrentCompany(medical);
        return ResponseEntity.created(URI.create("/api/medicals/" + saved.getMedicalId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalMaster> updateMedical(@PathVariable Long id, @RequestBody MedicalMaster medical) {
        MedicalMaster updated = medicalService.updateMedicalForCurrentCompany(id, medical);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteMedical(@PathVariable Long id) {
        boolean deleted = medicalService.softDeleteMedicalForCurrentCompany(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}