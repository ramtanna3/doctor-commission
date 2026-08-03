package com.org.app.dcas.controller;

import com.org.app.dcas.model.CommissionMaster;
import com.org.app.dcas.model.FileAudit;
import com.org.app.dcas.service.CommissionService;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/commissions")
public class CommissionController {

    private static final Logger log = LoggerFactory.getLogger(CommissionController.class);

    private final CommissionService commissionService;
    private final CompanyContext companyContext;

    public CommissionController(CommissionService commissionService, CompanyContext companyContext) {
        this.commissionService = commissionService;
        this.companyContext = companyContext;
    }

    @PostMapping("/create-with-products")
    public ResponseEntity<DoctorResponse> createDoctorWithProducts(@RequestBody DoctorWithProductsRequest req) {
        log.info("POST /api/commissions/create-with-products - creating doctor with products, name='{}'", req.getName());
        DoctorResponse response = commissionService.createDoctorWithProducts(req);
        log.info("POST /api/commissions/create-with-products - created doctorId={}", response.getId());
        return ResponseEntity.created(URI.create("/api/commissions/create-with-products/" + response.getId())).body(response);
    }

    @PostMapping("/process-sales-excel")
    public ResponseEntity<FileAudit> processSalesExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("distributorId") Long distributorId) {
        Long userId = companyContext.getUserId();
        String fileName = file.getOriginalFilename();
        log.info("POST /api/commissions/process-sales-excel - file='{}', distributorId={}, userId={}, size={} bytes",
                fileName, distributorId, userId, file.getSize());
        FileAudit result = commissionService.processSalesExcelWithMetrics(file, distributorId, userId, fileName);
        log.info("POST /api/commissions/process-sales-excel - completed fileAuditId={}, success={}, nigo={}, unmatched={}",
                result.getFileAuditId(), result.getSuccessCount(), result.getNigoCount(), result.getUnmatchedCount());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public List<CommissionMaster> getAllCommissions() {
        log.info("GET /api/commissions - fetching all commissions for current company");
        List<CommissionMaster> result = commissionService.getAllCommissionsForCurrentCompany();
        log.info("GET /api/commissions - returned {} commissions", result.size());
        return result;
    }

    @GetMapping("/active")
    public List<CommissionResponse> getAllActiveCommissions() {
        log.info("GET /api/commissions/active - fetching active commissions");
        List<CommissionResponse> result = commissionService.getActiveCommissionsForCurrentCompany();
        log.info("GET /api/commissions/active - returned {} commissions", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommissionResponse> getCommissionById(@PathVariable Long id) {
        log.info("GET /api/commissions/{} - fetching commission", id);
        CommissionResponse commission = commissionService.getCommissionByIdForCurrentCompany(id);
        if (commission == null) { log.warn("GET /api/commissions/{} - not found", id); }
        return commission != null ? ResponseEntity.ok(commission) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<CommissionMaster> createCommission(@RequestBody CommissionMaster commission) {
        log.info("POST /api/commissions - creating commission doctorId={}, medicalId={}, productId={}",
                commission.getDoctor() != null ? commission.getDoctor().getDoctorId() : null,
                commission.getMedical() != null ? commission.getMedical().getMedicalId() : null,
                commission.getProduct() != null ? commission.getProduct().getProductId() : null);
        CommissionMaster saved = commissionService.createCommissionForCurrentCompany(commission);
        log.info("POST /api/commissions - created commissionId={}", saved.getCommissionId());
        return ResponseEntity.created(URI.create("/api/commissions/" + saved.getCommissionId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommissionMaster> updateCommission(@PathVariable Long id, @RequestBody CommissionMaster commission) {
        log.info("PUT /api/commissions/{} - updating commission", id);
        CommissionMaster updated = commissionService.updateCommissionForCurrentCompany(id, commission);
        if (updated == null) { log.warn("PUT /api/commissions/{} - not found", id); }
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteCommission(@PathVariable Long id) {
        log.info("DELETE /api/commissions/{} - soft-deleting commission", id);
        boolean deleted = commissionService.softDeleteCommissionForCurrentCompany(id);
        if (!deleted) { log.warn("DELETE /api/commissions/{} - not found", id); }
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
