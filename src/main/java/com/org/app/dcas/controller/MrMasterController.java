package com.org.app.dcas.controller;

import com.org.app.dcas.model.MrMaster;
import com.org.app.dcas.service.MrMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/mrs")
public class MrMasterController {

    private final MrMasterService mrService;

    public MrMasterController(MrMasterService mrService) {
        this.mrService = mrService;
    }

    @GetMapping
    public List<MrMaster> getAllMrs() {
        return mrService.getAllMrForCurrentCompany();
    }

    @GetMapping("/active")
    public List<MrMaster> getAllActiveMrs() {
        return mrService.getActiveMrForCurrentCompany();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MrMaster> getMrById(@PathVariable Long id) {
        MrMaster mr = mrService.getMrByIdForCurrentCompany(id);
        return mr != null ? ResponseEntity.ok(mr) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<MrMaster> createMr(@RequestBody MrMaster mr) {
        MrMaster saved = mrService.createMrForCurrentCompany(mr);
        return ResponseEntity.created(URI.create("/api/mrs/" + saved.getMrId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MrMaster> updateMr(@PathVariable Long id, @RequestBody MrMaster mr) {
        MrMaster updated = mrService.updateMrForCurrentCompany(id, mr);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteMr(@PathVariable Long id) {
        boolean deleted = mrService.softDeleteMrForCurrentCompany(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}