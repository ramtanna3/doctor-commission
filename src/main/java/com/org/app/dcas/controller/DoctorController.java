package com.org.app.dcas.controller;

import com.org.app.dcas.model.DoctorMaster;
import com.org.app.dcas.service.DoctorService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public List<DoctorMaster> getAllDoctors() {
        return doctorService.getAllDoctorsForCurrentCompany();
    }

    @GetMapping("/active")
    public List<DoctorMaster> getAllActiveDoctors() {
        return doctorService.getActiveDoctorsForCurrentCompany();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorMaster> getDoctorById(@PathVariable Long id) {
        DoctorMaster doctor = doctorService.getDoctorByIdForCurrentCompany(id);
        return doctor != null ? ResponseEntity.ok(doctor) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<DoctorMaster> createDoctor(@RequestBody DoctorMaster doctor) {
        DoctorMaster saved = doctorService.createDoctorForCurrentCompany(doctor);
        return ResponseEntity.created(URI.create("/api/doctors/" + saved.getDoctorId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorMaster> updateDoctor(@PathVariable Long id, @RequestBody DoctorMaster doctor) {
        DoctorMaster updated = doctorService.updateDoctorForCurrentCompany(id, doctor);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteDoctor(@PathVariable Long id) {
        boolean deleted = doctorService.softDeleteDoctorForCurrentCompany(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
