package com.org.app.dcas.controller;

import com.org.app.dcas.dto.SalesTransactionDoctorAssignmentRequest;
import com.org.app.dcas.dto.SalesTransactionDoctorAssignmentResult;
import com.org.app.dcas.dto.SalesTransactionListResponse;
import com.org.app.dcas.service.SalesTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-transactions")
public class SalesTransactionController {

    private static final Logger log = LoggerFactory.getLogger(SalesTransactionController.class);

    private final SalesTransactionService salesTransactionService;

    public SalesTransactionController(SalesTransactionService salesTransactionService) {
        this.salesTransactionService = salesTransactionService;
    }

    @GetMapping("/by-distributor/{distributorId}/{matched}")
    public SalesTransactionListResponse getSalesTransactionsByDistributor(
            @PathVariable Long distributorId,
            @PathVariable String matched
    ) {
        log.info("GET /api/sales-transactions/by-distributor/{}/{} - fetching transactions", distributorId, matched);
        SalesTransactionListResponse result = salesTransactionService.getSalesTransactionsByDistributor(distributorId, matched);
        int count = result.getTransactions() != null ? result.getTransactions().size() : 0;
        log.info("GET /api/sales-transactions/by-distributor/{}/{} - returned {} transactions", distributorId, matched, count);
        return result;
    }

    @PutMapping("/assign-doctor")
    public ResponseEntity<SalesTransactionDoctorAssignmentResult> assignDoctorToSalesTransactions(
            @RequestBody SalesTransactionDoctorAssignmentRequest request
    ) {
        log.info("PUT /api/sales-transactions/assign-doctor - assigning doctorId={} to {} transactions",
                request.getDoctorId(), request.getSalesTransactionIds().size());
        SalesTransactionDoctorAssignmentResult result = salesTransactionService.assignDoctorToSalesTransactions(
                request.getSalesTransactionIds(), request.getDoctorId());
        log.info("PUT /api/sales-transactions/assign-doctor - assigned={}, failed={}",
                result.getAssignedSalesTransactionIds().size(), result.getFailedSalesTransactionIds().size());
        return ResponseEntity.ok(result);
    }
}
