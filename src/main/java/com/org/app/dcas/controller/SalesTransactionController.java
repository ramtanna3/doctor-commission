package com.org.app.dcas.controller;

import com.org.app.dcas.dto.SalesTransactionListResponse;
import com.org.app.dcas.service.SalesTransactionService;
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
}
