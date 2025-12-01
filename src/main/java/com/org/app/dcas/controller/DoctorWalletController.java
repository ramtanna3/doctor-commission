package com.org.app.dcas.controller;

import com.org.app.dcas.dto.DoctorWalletBalanceResponse;
import com.org.app.dcas.dto.DoctorWalletLedgerListResponse;
import com.org.app.dcas.dto.DoctorWalletTransactionRequest;
import com.org.app.dcas.model.DoctorWalletLedger;
import com.org.app.dcas.service.DoctorWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/doctor-wallet")
public class DoctorWalletController {

    private final DoctorWalletService doctorWalletService;

    @Autowired
    public DoctorWalletController(DoctorWalletService doctorWalletService) {
        this.doctorWalletService = doctorWalletService;
    }

    @PostMapping("/add-transaction")
    public ResponseEntity<DoctorWalletLedger> addTransaction(@RequestBody DoctorWalletTransactionRequest request) {
        DoctorWalletLedger ledger = doctorWalletService.addTransaction(
                request.getDoctorId(),
                request.getReferenceType(),
                request.getCreditAmount(),
                request.getDebitAmount(),
                request.getRemarks(),
                request.getReferenceId()
        );
        return ResponseEntity.ok(ledger);
    }

    @GetMapping("/balance/{doctorId}")
    public ResponseEntity<DoctorWalletBalanceResponse> getWalletBalance(@PathVariable Long doctorId) {
        DoctorWalletBalanceResponse response = doctorWalletService.getWalletBalance(doctorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance/company")
    public ResponseEntity<List<DoctorWalletBalanceResponse>> getAllDoctorBalancesForCompany() {
        List<DoctorWalletBalanceResponse> responses = doctorWalletService.getAllDoctorBalancesForCompany();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/ledger/{doctorId}")
    public ResponseEntity<DoctorWalletLedgerListResponse> getLedgerEntries(@PathVariable Long doctorId) {
        DoctorWalletLedgerListResponse response = doctorWalletService.getLedgerEntries(doctorId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync/all")
    public ResponseEntity<String> syncWalletForAllDoctors() {
        int count = doctorWalletService.syncWalletForAllDoctors();
        return ResponseEntity.ok("Synced wallet for " + count + " sale transactions.");
    }

    @PostMapping("/sync/{doctorId}")
    public ResponseEntity<String> syncWalletForDoctor(@PathVariable Long doctorId) {
        int count = doctorWalletService.syncWalletForDoctor(doctorId);
        return ResponseEntity.ok("Synced wallet for " + count + " sale transactions for doctorId " + doctorId + ".");
    }
}
