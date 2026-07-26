package com.org.app.dcas.controller;

import com.org.app.dcas.service.CommissionCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/commission-calculator")
public class CommissionCalculatorController {

    private static final Logger log = LoggerFactory.getLogger(CommissionCalculatorController.class);

    private final CommissionCalculator commissionCalculator;

    @Autowired
    public CommissionCalculatorController(
            CommissionCalculator commissionCalculator
    ) {
        this.commissionCalculator = commissionCalculator;
    }

    @PostMapping("/rematch")
    public ResponseEntity<com.org.app.dcas.dto.RematchResult> rematchSalesTransactions(@RequestBody List<Long> salesTransactionIds) {
        log.info("POST /api/commission-calculator/rematch - rematching {} transactions", salesTransactionIds.size());
        com.org.app.dcas.dto.RematchResult result = commissionCalculator.rematchAndUpdateSalesTransactions(salesTransactionIds);
        log.info("POST /api/commission-calculator/rematch - success={}, failed={}",
                result.getSuccessIds().size(), result.getFailedIds().size());
        return ResponseEntity.ok(result);
    }
}
