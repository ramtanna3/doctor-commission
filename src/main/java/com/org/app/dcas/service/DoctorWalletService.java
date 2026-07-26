package com.org.app.dcas.service;

import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.dto.DoctorWalletBalanceResponse;
import com.org.app.dcas.dto.DoctorWalletLedgerListResponse;
import com.org.app.dcas.dto.DoctorPayoutRequest;
import com.org.app.dcas.dto.DoctorWalletLedgerResponse;
import com.org.app.dcas.model.DoctorMaster;
import com.org.app.dcas.model.DoctorWalletLedger;
import com.org.app.dcas.model.SalesTransaction;
import com.org.app.dcas.repository.DoctorWalletLedgerRepository;
import com.org.app.dcas.repository.DoctorMasterRepository;
import com.org.app.dcas.repository.SalesTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DoctorWalletService {

    private static final Logger log = LoggerFactory.getLogger(DoctorWalletService.class);

    @Transactional
    public int payoutCommission(DoctorPayoutRequest request) {
        int count = 0;
        if (request == null || request.getDoctorBalances() == null) return 0;
        log.info("payoutCommission - processing {} doctors", request.getDoctorBalances().size());
        for (DoctorPayoutRequest.DoctorBalance db : request.getDoctorBalances()) {
            if (db.doctorId != null && db.balance < 0) {
                DoctorMaster doctor = doctorMasterRepository.findById(db.doctorId).orElse(null);
                if (doctor != null) {
                    log.info("payoutCommission - creating payout for doctorId={}, balance={}", db.doctorId, db.balance);
                    DoctorWalletLedger ledger = new DoctorWalletLedger();
                    ledger.setDoctor(doctor);
                    ledger.setTransactionDate(LocalDate.now());
                    ledger.setReferenceType(DoctorWalletLedger.ReferenceType.PAYOUT);
                    ledger.setCreditAmount(BigDecimal.valueOf(Math.abs(db.balance)));
                    ledger.setDebitAmount(BigDecimal.ZERO);
                    ledger.setRemarks("Payout commission for negative balance");
                    String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
                    ledger.setCreatedBy(userIdStr);
                    walletLedgerRepository.save(ledger);
                    count++;
                }
            }
        }
        log.info("payoutCommission - created {} payout entries", count);
        return count;
    }

    private final DoctorWalletLedgerRepository walletLedgerRepository;
    private final DoctorMasterRepository doctorMasterRepository;
    private final CompanyContext companyContext;
    private final SalesTransactionRepository salesTransactionRepository;

    @Autowired
    public DoctorWalletService(
        DoctorWalletLedgerRepository walletLedgerRepository,
        DoctorMasterRepository doctorMasterRepository,
        CompanyContext companyContext,
        SalesTransactionRepository salesTransactionRepository
    ) {
        this.walletLedgerRepository = walletLedgerRepository;
        this.doctorMasterRepository = doctorMasterRepository;
        this.companyContext = companyContext;
        this.salesTransactionRepository = salesTransactionRepository;
    }

    @Transactional
    public void syncDoctorWalletLedger(SalesTransaction tx, String createdBy) {
        if (tx.getDoctor() != null && tx.getCommissionAmount() != null
            && tx.getCommissionAmount().compareTo(BigDecimal.ZERO) > 0) {
            log.info("syncDoctorWalletLedger - salesTxId={}, doctorId={}, commissionAmount={}",
                    tx.getSalesTransactionId(),
                    tx.getDoctor().getDoctorId(),
                    tx.getCommissionAmount());
            DoctorWalletLedger ledger = new DoctorWalletLedger();
            ledger.setDoctor(tx.getDoctor());
            ledger.setTransactionDate(tx.getDate() != null ? tx.getDate() : LocalDate.now());
            ledger.setReferenceType(DoctorWalletLedger.ReferenceType.SALE_COMMISSION);
            ledger.setReferenceId(tx.getSalesTransactionId());
            ledger.setCreditAmount(BigDecimal.ZERO);
            ledger.setDebitAmount(tx.getCommissionAmount());
            ledger.setSalesTransaction(tx);
            ledger.setRemarks("Commission for sale, voucher: " + tx.getVoucherNo());
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            ledger.setCreatedBy(userIdStr);
            walletLedgerRepository.save(ledger);
            log.info("syncDoctorWalletLedger - ledger entry saved for salesTxId={}", tx.getSalesTransactionId());
        } else {
            log.debug("syncDoctorWalletLedger - skipping salesTxId={} (no doctor or zero commission)",
                    tx.getSalesTransactionId());
        }
    }

    @Transactional
    public DoctorWalletLedger addTransaction(Long doctorId, DoctorWalletLedger.ReferenceType type, BigDecimal credit, BigDecimal debit, String remarks, Long referenceId) {
        log.info("addTransaction - doctorId={}, type={}, credit={}, debit={}", doctorId, type, credit, debit);
        DoctorMaster doctor = doctorMasterRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        DoctorWalletLedger ledger = new DoctorWalletLedger();
        ledger.setDoctor(doctor);
        ledger.setTransactionDate(LocalDate.now());
        ledger.setReferenceType(type);
        ledger.setCreditAmount(credit != null ? credit : BigDecimal.ZERO);
        ledger.setDebitAmount(debit != null ? debit : BigDecimal.ZERO);
        ledger.setRemarks(remarks);
        ledger.setReferenceId(referenceId);
        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
        ledger.setCreatedBy(userIdStr);
        DoctorWalletLedger saved = walletLedgerRepository.save(ledger);
        log.info("addTransaction - saved ledgerId={}", saved.getDoctorWalletLedgerId());
        return saved;
    }

    public DoctorWalletBalanceResponse getWalletBalance(Long doctorId) {
        log.info("getWalletBalance - doctorId={}", doctorId);
        List<DoctorWalletLedger> ledgers = walletLedgerRepository.findByDoctorId(doctorId);
        double balance = 0.0;
        DoctorMaster doctor = null;
        if (!ledgers.isEmpty()) {
            doctor = ledgers.get(0).getDoctor();
        } else {
            doctor = doctorMasterRepository.findById(doctorId).orElse(null);
        }
        for (DoctorWalletLedger ledger : ledgers) {
            balance += ledger.getCreditAmount() != null ? ledger.getCreditAmount().doubleValue() : 0.0;
            balance -= ledger.getDebitAmount() != null ? ledger.getDebitAmount().doubleValue() : 0.0;
        }
        log.info("getWalletBalance - doctorId={}, balance={}, ledgerEntries={}", doctorId, balance, ledgers.size());
        return new DoctorWalletBalanceResponse(doctor, balance);
    }

    public DoctorWalletLedgerListResponse getLedgerEntries(Long doctorId) {
        log.info("getLedgerEntries - doctorId={}", doctorId);
        List<DoctorWalletLedger> ledgers = walletLedgerRepository.findByDoctorId(doctorId);
        DoctorMaster doctor = null;
        if (!ledgers.isEmpty()) {
            doctor = ledgers.get(0).getDoctor();
        } else {
            doctor = doctorMasterRepository.findById(doctorId).orElse(null);
        }
        List<DoctorWalletLedgerResponse> transactions = new java.util.ArrayList<>();
        for (DoctorWalletLedger ledger : ledgers) {
            transactions.add(DoctorWalletLedgerResponse.from(ledger));
        }
        log.info("getLedgerEntries - doctorId={}, returned {} entries", doctorId, transactions.size());
        return new DoctorWalletLedgerListResponse(doctor, transactions);
    }

    @Transactional
    public int syncWalletForAllDoctors() {
        Long companyId = companyContext.getCompanyId();
        log.info("syncWalletForAllDoctors - companyId={}", companyId);
        Set<Long> doctorIds = salesTransactionRepository.findDistinctDoctorIdsByCompanyId(companyId);
        log.info("syncWalletForAllDoctors - found {} distinct doctors with transactions", doctorIds.size());
        int totalCount = 0;
        for (Long doctorId : doctorIds) {
            totalCount += syncWalletForDoctor(doctorId);
        }
        log.info("syncWalletForAllDoctors - synced {} total transactions", totalCount);
        return totalCount;
    }

    @Transactional
    public int syncWalletForDoctor(Long doctorId) {
        log.info("syncWalletForDoctor - doctorId={}", doctorId);
        int count = 0;
        List<SalesTransaction> transactions = salesTransactionRepository.findByDoctorIdAndCompanyId(doctorId, companyContext.getCompanyId());
        log.info("syncWalletForDoctor - doctorId={}, {} matched transactions to evaluate", doctorId, transactions.size());
        for (SalesTransaction tx : transactions) {
            if (Boolean.TRUE.equals(tx.getIsMatched()) && tx.getCommissionAmount() != null) {
                boolean exists = walletLedgerRepository.existsByReferenceIdAndReferenceType(tx.getSalesTransactionId(), DoctorWalletLedger.ReferenceType.SALE_COMMISSION);
                if (!exists) {
                    syncDoctorWalletLedger(tx, companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system");
                    count++;
                } else {
                    log.debug("syncWalletForDoctor - skipping salesTxId={}, ledger already exists", tx.getSalesTransactionId());
                }
            }
        }
        log.info("syncWalletForDoctor - doctorId={}, synced {} new transactions", doctorId, count);
        return count;
    }

    public List<DoctorWalletBalanceResponse> getAllDoctorBalancesForCompany() {
        Long companyId = companyContext.getCompanyId();
        log.info("getAllDoctorBalancesForCompany - companyId={}", companyId);
        List<DoctorMaster> doctors = doctorMasterRepository.findByCompanyId(companyId);
        if (doctors.isEmpty()) return java.util.Collections.emptyList();

        List<Long> doctorIds = doctors.stream()
                .map(DoctorMaster::getDoctorId)
                .collect(Collectors.toList());

        log.info("getAllDoctorBalancesForCompany - fetching balances for {} doctors via GROUP BY query", doctors.size());
        Map<Long, Double> balanceMap = new HashMap<>();
        for (Object[] row : walletLedgerRepository.findBalancesByDoctorIds(doctorIds)) {
            Long doctorId = ((Number) row[0]).longValue();
            Double balance = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            balanceMap.put(doctorId, balance);
        }

        List<DoctorWalletBalanceResponse> responses = new java.util.ArrayList<>();
        for (DoctorMaster doctor : doctors) {
            double balance = balanceMap.getOrDefault(doctor.getDoctorId(), 0.0);
            responses.add(new DoctorWalletBalanceResponse(doctor, balance));
        }
        log.info("getAllDoctorBalancesForCompany - returned {} balances", responses.size());
        return responses;
    }

}
