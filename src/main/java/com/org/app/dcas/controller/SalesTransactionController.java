package com.org.app.dcas.controller;

import com.org.app.dcas.dto.SalesTransactionDoctorAssignmentRequest;
import com.org.app.dcas.dto.SalesTransactionDoctorAssignmentResult;
import com.org.app.dcas.dto.SalesTransactionListResponse;
import com.org.app.dcas.service.SalesTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales-transactions")
public class SalesTransactionController {

    private final SalesTransactionService salesTransactionService;

    public SalesTransactionController(SalesTransactionService salesTransactionService) {
        this.salesTransactionService = salesTransactionService;
    }

    @GetMapping("/by-distributor/{distributorId}/{matched}")
    public SalesTransactionListResponse getSalesTransactionsByDistributor(
            @PathVariable Long distributorId,
            @PathVariable String matched
    ) {
        return salesTransactionService.getSalesTransactionsByDistributor(distributorId, matched);
    }

    @PutMapping("/assign-doctor")
    public ResponseEntity<SalesTransactionDoctorAssignmentResult> assignDoctorToSalesTransactions(
            @RequestBody SalesTransactionDoctorAssignmentRequest request
    ) {
        SalesTransactionDoctorAssignmentResult result = salesTransactionService.assignDoctorToSalesTransactions(
                request.getSalesTransactionIds(), request.getDoctorId());
        return ResponseEntity.ok(result);
    }
}
