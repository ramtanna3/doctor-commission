package com.org.app.dcas.service;

import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.dto.SalesTransactionDoctorAssignmentResult;
import com.org.app.dcas.dto.SalesTransactionResponse;
import com.org.app.dcas.dto.SalesTransactionListResponse;
import com.org.app.dcas.model.DoctorMaster;
import com.org.app.dcas.model.Distributor;
import com.org.app.dcas.model.SalesTransaction;
import com.org.app.dcas.repository.DoctorMasterRepository;
import com.org.app.dcas.repository.DistributorRepository;
import com.org.app.dcas.repository.SalesTransactionRepository;
import com.org.app.dcas.service.DoctorWalletService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesTransactionService {

    private final SalesTransactionRepository salesTransactionRepository;
    private final DistributorRepository distributorRepository;
    private final DoctorMasterRepository doctorMasterRepository;
    private final CompanyContext companyContext;
    private final DoctorWalletService doctorWalletService;

    public SalesTransactionService(SalesTransactionRepository salesTransactionRepository,
                                  DistributorRepository distributorRepository,
                                  DoctorMasterRepository doctorMasterRepository,
                                  CompanyContext companyContext,
                                  DoctorWalletService doctorWalletService) {
        this.salesTransactionRepository = salesTransactionRepository;
        this.distributorRepository = distributorRepository;
        this.doctorMasterRepository = doctorMasterRepository;
        this.companyContext = companyContext;
        this.doctorWalletService = doctorWalletService;
    }

    public List<SalesTransaction> getAllByDistributor(Long distributorId) {
        return salesTransactionRepository.findByDistributorDistributorId(distributorId);
    }

    public List<SalesTransaction> getMatchedByDistributor(Long distributorId) {
        return salesTransactionRepository.findByDistributorDistributorIdAndIsMatchedTrue(distributorId);
    }

    public List<SalesTransaction> getUnmatchedByDistributor(Long distributorId) {
        return salesTransactionRepository.findByDistributorDistributorIdAndIsMatchedFalse(distributorId);
    }

    public SalesTransactionListResponse getSalesTransactionsByDistributor(Long distributorId, String matched) {
        List<SalesTransaction> txs;
        switch (matched.toUpperCase()) {
            case "ALL":
                txs = getAllByDistributor(distributorId);
                break;
            case "MATCHED":
                txs = getMatchedByDistributor(distributorId);
                break;
            case "UNMATCHED":
                txs = getUnmatchedByDistributor(distributorId);
                break;
            default:
                throw new IllegalArgumentException("Invalid matched value. Use ALL, MATCHED, or UNMATCHED.");
        }
        Distributor distributor = distributorRepository.findById(distributorId).orElse(null);
        List<SalesTransactionResponse> transactions = txs.stream()
            .map(SalesTransactionResponse::from)
            .collect(Collectors.toList());
        return new SalesTransactionListResponse(distributor, transactions);
    }

    public SalesTransactionDoctorAssignmentResult assignDoctorToSalesTransactions(List<Long> salesTransactionIds, Long doctorId) {
        DoctorMaster doctor = doctorMasterRepository.findByDoctorIdAndCompanyId(doctorId, companyContext.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found for id: " + doctorId));

        List<SalesTransaction> transactions = salesTransactionRepository.findAllById(salesTransactionIds);
        List<Long> assignedIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();
        String updatedBy = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";

        for (SalesTransaction tx : transactions) {
            if (tx == null) {
                continue;
            }
            if (tx.getCompany() == null || !tx.getCompany().getCompanyId().equals(companyContext.getCompanyId())) {
                failedIds.add(tx.getSalesTransactionId());
                continue;
            }
            boolean isCurrentlyMatched = Boolean.TRUE.equals(tx.getIsMatched());
            boolean hasDoctor = tx.getDoctor() != null;
            if (isCurrentlyMatched && hasDoctor) {
                failedIds.add(tx.getSalesTransactionId());
                continue;
            }

            tx.setDoctor(doctor);
            tx.setIsMatched(true);

            if (tx.getCommissionPercent() == null || tx.getCommissionPercent().compareTo(BigDecimal.ZERO) <= 0) {
                if (tx.getProduct() != null && tx.getProduct().getDefaultCommissionPercentage() != null
                        && tx.getProduct().getDefaultCommissionPercentage().compareTo(BigDecimal.ZERO) > 0) {
                    tx.setCommissionPercent(tx.getProduct().getDefaultCommissionPercentage());
                }
            }

            if ((tx.getCommissionAmount() == null || tx.getCommissionAmount().compareTo(BigDecimal.ZERO) <= 0)
                    && tx.getCommissionPercent() != null
                    && tx.getCommissionPercent().compareTo(BigDecimal.ZERO) > 0
                    && tx.getAmount() != null && !tx.getAmount().isEmpty()) {
                try {
                    BigDecimal amount = new BigDecimal(tx.getAmount());
                    BigDecimal commissionAmount = amount.multiply(tx.getCommissionPercent()).divide(new BigDecimal("100"));
                    tx.setCommissionAmount(commissionAmount);
                } catch (Exception ignored) {
                }
            }

            tx.setUpdatedBy(updatedBy);
            salesTransactionRepository.save(tx);
            doctorWalletService.syncDoctorWalletLedger(tx, updatedBy);
            assignedIds.add(tx.getSalesTransactionId());
        }

        return new SalesTransactionDoctorAssignmentResult(assignedIds, failedIds);
    }
}
