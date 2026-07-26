package com.org.app.dcas.controller;

import com.org.app.dcas.model.DoctorMaster;
import com.org.app.dcas.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private static final Logger log = LoggerFactory.getLogger(DoctorController.class);

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public List<DoctorMaster> getAllDoctors() {
        log.info("GET /api/doctors - fetching all doctors for current company");
        List<DoctorMaster> result = doctorService.getAllDoctorsForCurrentCompany();
        log.info("GET /api/doctors - returned {} doctors", result.size());
        return result;
    }

    @GetMapping("/active")
    public List<DoctorMaster> getAllActiveDoctors() {
        log.info("GET /api/doctors/active - fetching active doctors");
        List<DoctorMaster> result = doctorService.getActiveDoctorsForCurrentCompany();
        log.info("GET /api/doctors/active - returned {} active doctors", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorMaster> getDoctorById(@PathVariable Long id) {
        log.info("GET /api/doctors/{} - fetching doctor", id);
        DoctorMaster doctor = doctorService.getDoctorByIdForCurrentCompany(id);
        if (doctor == null) { log.warn("GET /api/doctors/{} - not found", id); }
        return doctor != null ? ResponseEntity.ok(doctor) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<DoctorMaster> createDoctor(@RequestBody DoctorMaster doctor) {
        log.info("POST /api/doctors - creating doctor name='{}'", doctor.getName());
        DoctorMaster saved = doctorService.createDoctorForCurrentCompany(doctor);
        log.info("POST /api/doctors - created doctorId={}", saved.getDoctorId());
        return ResponseEntity.created(URI.create("/api/doctors/" + saved.getDoctorId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorMaster> updateDoctor(@PathVariable Long id, @RequestBody DoctorMaster doctor) {
        log.info("PUT /api/doctors/{} - updating doctor", id);
        DoctorMaster updated = doctorService.updateDoctorForCurrentCompany(id, doctor);
        if (updated == null) { log.warn("PUT /api/doctors/{} - not found", id); }
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteDoctor(@PathVariable Long id) {
        log.info("DELETE /api/doctors/{} - soft-deleting doctor", id);
        boolean deleted = doctorService.softDeleteDoctorForCurrentCompany(id);
        if (!deleted) { log.warn("DELETE /api/doctors/{} - not found", id); }
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
