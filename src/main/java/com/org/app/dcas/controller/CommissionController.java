package com.org.app.dcas.controller;

import com.org.app.dcas.model.CommissionMaster;
import com.org.app.dcas.model.FileAudit;
import com.org.app.dcas.service.CommissionService;
import com.org.app.dcas.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/commissions")
public class CommissionController {

    private final CommissionService commissionService;

    public CommissionController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    @PostMapping("/create-with-products")
    public ResponseEntity<DoctorResponse> createDoctorWithProducts(@RequestBody DoctorWithProductsRequest req) {
        DoctorResponse response = commissionService.createDoctorWithProducts(req);
        return ResponseEntity.created(URI.create("/api/commissions/create-with-products/" + response.getId())).body(response);
    }


    @PostMapping("/process-sales-excel")
    public ResponseEntity<FileAudit> processSalesExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("distributorId") Long distributorId,
            @RequestHeader(value = "x-user-id") Long userId) {
        String fileName = file.getOriginalFilename();
        FileAudit result = commissionService.processSalesExcelWithMetrics(file, distributorId, userId, fileName);
        return ResponseEntity.ok(result);
    }


    @GetMapping
    public List<CommissionMaster> getAllCommissions() {
        return commissionService.getAllCommissionsForCurrentCompany();
    }

    @GetMapping("/active")
    public List<CommissionResponse> getAllActiveCommissions() {
        return commissionService.getActiveCommissionsForCurrentCompany();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommissionResponse> getCommissionById(@PathVariable Long id) {
        CommissionResponse commission = commissionService.getCommissionByIdForCurrentCompany(id);
        return commission != null ? ResponseEntity.ok(commission) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<CommissionMaster> createCommission(@RequestBody CommissionMaster commission) {
        CommissionMaster saved = commissionService.createCommissionForCurrentCompany(commission);
        return ResponseEntity.created(URI.create("/api/commissions/" + saved.getCommissionId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommissionMaster> updateCommission(@PathVariable Long id, @RequestBody CommissionMaster commission) {
        CommissionMaster updated = commissionService.updateCommissionForCurrentCompany(id, commission);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteCommission(@PathVariable Long id) {
        boolean deleted = commissionService.softDeleteCommissionForCurrentCompany(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
