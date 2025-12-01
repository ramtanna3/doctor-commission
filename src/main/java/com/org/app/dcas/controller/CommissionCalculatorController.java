package com.org.app.dcas.controller;

import com.org.app.dcas.service.CommissionCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/commission-calculator")
public class CommissionCalculatorController {

    private final CommissionCalculator commissionCalculator;

    @Autowired
    public CommissionCalculatorController(
            CommissionCalculator commissionCalculator
    ) {
        this.commissionCalculator = commissionCalculator;
    }

    @PostMapping("/rematch")
    public ResponseEntity<com.org.app.dcas.dto.RematchResult> rematchSalesTransactions(@RequestBody List<Long> salesTransactionIds) {
        com.org.app.dcas.dto.RematchResult result = commissionCalculator.rematchAndUpdateSalesTransactions(salesTransactionIds);
        return ResponseEntity.ok(result);
    }
}
