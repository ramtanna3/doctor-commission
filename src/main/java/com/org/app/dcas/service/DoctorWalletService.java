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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class DoctorWalletService {

    @Transactional
    public int payoutCommission(DoctorPayoutRequest request) {
        int count = 0;
        if (request == null || request.getDoctorBalances() == null) return 0;
        for (DoctorPayoutRequest.DoctorBalance db : request.getDoctorBalances()) {
            if (db.doctorId != null && db.balance < 0) {
                DoctorMaster doctor = doctorMasterRepository.findById(db.doctorId)
                        .orElse(null);
                if (doctor != null) {
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
        }
    }

    @Transactional
    public DoctorWalletLedger addTransaction(Long doctorId, DoctorWalletLedger.ReferenceType type, BigDecimal credit, BigDecimal debit, String remarks, Long referenceId) {
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
        return walletLedgerRepository.save(ledger);
    }

    public DoctorWalletBalanceResponse getWalletBalance(Long doctorId) {
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
        return new DoctorWalletBalanceResponse(doctor, balance);
    }

    public DoctorWalletLedgerListResponse getLedgerEntries(Long doctorId) {
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
        return new DoctorWalletLedgerListResponse(doctor, transactions);
    }

    @Transactional
    public int syncWalletForAllDoctors() {
        Long companyId = companyContext.getCompanyId();
        // Get distinct doctor IDs for the company from sales transactions
        Set<Long> doctorIds = salesTransactionRepository.findDistinctDoctorIdsByCompanyId(companyId);

        int totalCount = 0;
        for (Long doctorId : doctorIds) {
            totalCount += syncWalletForDoctor(doctorId);
        }
        return totalCount;
    }

    @Transactional
    public int syncWalletForDoctor(Long doctorId) {
        int count = 0;
        List<SalesTransaction> transactions = salesTransactionRepository.findByDoctorIdAndCompanyId(doctorId, companyContext.getCompanyId());
        for (SalesTransaction tx : transactions) {
            if (Boolean.TRUE.equals(tx.getIsMatched()) && tx.getCommissionAmount() != null) {
                boolean exists = walletLedgerRepository.existsByReferenceIdAndReferenceType(tx.getSalesTransactionId(), DoctorWalletLedger.ReferenceType.SALE_COMMISSION);
                if (!exists) {
                    syncDoctorWalletLedger(tx, companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system");
                    count++;
                }
            }
        }
        return count;
    }

    public List<DoctorWalletBalanceResponse> getAllDoctorBalancesForCompany() {
        Long companyId = companyContext.getCompanyId();
        List<DoctorMaster> doctors = doctorMasterRepository.findByCompanyId(companyId);
        List<DoctorWalletBalanceResponse> responses = new java.util.ArrayList<>();
        for (DoctorMaster doctor : doctors) {
            responses.add(getWalletBalance(doctor.getDoctorId()));
        }
        return responses;
    }

}
