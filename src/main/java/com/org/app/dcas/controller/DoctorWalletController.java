package com.org.app.dcas.controller;

import com.org.app.dcas.dto.DoctorWalletBalanceResponse;
import com.org.app.dcas.dto.DoctorPayoutRequest;
import com.org.app.dcas.dto.DoctorWalletLedgerListResponse;
import com.org.app.dcas.dto.DoctorWalletTransactionRequest;
import com.org.app.dcas.model.DoctorWalletLedger;
import com.org.app.dcas.service.DoctorWalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/doctor-wallet")
public class DoctorWalletController {

    private static final Logger log = LoggerFactory.getLogger(DoctorWalletController.class);

    private final DoctorWalletService doctorWalletService;

    @Autowired
    public DoctorWalletController(DoctorWalletService doctorWalletService) {
        this.doctorWalletService = doctorWalletService;
    }

    @PostMapping("/payout-commission")
    public ResponseEntity<String> payoutCommission(@RequestBody DoctorPayoutRequest request) {
        int size = request.getDoctorBalances() != null ? request.getDoctorBalances().size() : 0;
        log.info("POST /api/doctor-wallet/payout-commission - processing payout for {} doctors", size);
        int count = doctorWalletService.payoutCommission(request);
        log.info("POST /api/doctor-wallet/payout-commission - created {} payout entries", count);
        return ResponseEntity.ok("Payout commission entries created: " + count);
    }

    @PostMapping("/add-transaction")
    public ResponseEntity<DoctorWalletLedger> addTransaction(@RequestBody DoctorWalletTransactionRequest request) {
        log.info("POST /api/doctor-wallet/add-transaction - doctorId={}, type={}, credit={}, debit={}",
                request.getDoctorId(), request.getReferenceType(), request.getCreditAmount(), request.getDebitAmount());
        DoctorWalletLedger ledger = doctorWalletService.addTransaction(
                request.getDoctorId(), request.getReferenceType(),
                request.getCreditAmount(), request.getDebitAmount(),
                request.getRemarks(), request.getReferenceId());
        log.info("POST /api/doctor-wallet/add-transaction - created ledgerId={}", ledger.getDoctorWalletLedgerId());
        return ResponseEntity.ok(ledger);
    }

    @GetMapping("/balance/{doctorId}")
    public ResponseEntity<DoctorWalletBalanceResponse> getWalletBalance(@PathVariable Long doctorId) {
        log.info("GET /api/doctor-wallet/balance/{} - fetching wallet balance", doctorId);
        DoctorWalletBalanceResponse response = doctorWalletService.getWalletBalance(doctorId);
        log.info("GET /api/doctor-wallet/balance/{} - balance={}", doctorId, response.getBalance());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance/company")
    public ResponseEntity<List<DoctorWalletBalanceResponse>> getAllDoctorBalancesForCompany() {
        log.info("GET /api/doctor-wallet/balance/company - fetching all doctor balances");
        List<DoctorWalletBalanceResponse> responses = doctorWalletService.getAllDoctorBalancesForCompany();
        log.info("GET /api/doctor-wallet/balance/company - returned {} doctor balances", responses.size());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/ledger/{doctorId}")
    public ResponseEntity<DoctorWalletLedgerListResponse> getLedgerEntries(@PathVariable Long doctorId) {
        log.info("GET /api/doctor-wallet/ledger/{} - fetching ledger entries", doctorId);
        DoctorWalletLedgerListResponse response = doctorWalletService.getLedgerEntries(doctorId);
        int count = response.getTransactions() != null ? response.getTransactions().size() : 0;
        log.info("GET /api/doctor-wallet/ledger/{} - returned {} ledger entries", doctorId, count);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync/all")
    public ResponseEntity<String> syncWalletForAllDoctors() {
        log.info("POST /api/doctor-wallet/sync/all - syncing wallet for all doctors");
        int count = doctorWalletService.syncWalletForAllDoctors();
        log.info("POST /api/doctor-wallet/sync/all - synced {} sale transactions", count);
        return ResponseEntity.ok("Synced wallet for " + count + " sale transactions.");
    }

    @PostMapping("/sync/{doctorId}")
    public ResponseEntity<String> syncWalletForDoctor(@PathVariable Long doctorId) {
        log.info("POST /api/doctor-wallet/sync/{} - syncing wallet", doctorId);
        int count = doctorWalletService.syncWalletForDoctor(doctorId);
        log.info("POST /api/doctor-wallet/sync/{} - synced {} sale transactions", doctorId, count);
        return ResponseEntity.ok("Synced wallet for " + count + " sale transactions for doctorId " + doctorId + ".");
    }
}
