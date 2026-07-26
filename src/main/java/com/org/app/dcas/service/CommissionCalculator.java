package com.org.app.dcas.service;

import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.dto.SalesExcelRow;
import com.org.app.dcas.model.*;
import com.org.app.dcas.repository.CommissionMasterRepository;
import com.org.app.dcas.repository.MedicalMasterRepository;
import com.org.app.dcas.repository.ProductMasterRepository;
import com.org.app.dcas.repository.DistributorRepository;
import com.org.app.dcas.repository.SalesTransactionRepository;
import com.org.app.dcas.repository.CompanyRepository;
import com.org.app.dcas.repository.NigoSalesTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CommissionCalculator {

    private static final Logger log = LoggerFactory.getLogger(CommissionCalculator.class);

    private final CommissionMasterRepository commissionMasterRepository;
    private final MedicalMasterRepository medicalMasterRepository;
    private final ProductMasterRepository productMasterRepository;
    private final CompanyContext companyContext;
    private final DistributorRepository distributorRepository;
    private final SalesTransactionRepository salesTransactionRepository;
    private final CompanyRepository companyRepository;
    private final NigoSalesTransactionRepository nigoSalesTransactionRepository;
    private final DoctorWalletService doctorWalletService;

    public CommissionCalculator(
            CommissionMasterRepository commissionMasterRepository,
            MedicalMasterRepository medicalMasterRepository,
            ProductMasterRepository productMasterRepository,
            CompanyContext companyContext,
            DistributorRepository distributorRepository,
            SalesTransactionRepository salesTransactionRepository,
            CompanyRepository companyRepository,
            NigoSalesTransactionRepository nigoSalesTransactionRepository,
            DoctorWalletService doctorWalletService
    ) {
        this.commissionMasterRepository = commissionMasterRepository;
        this.medicalMasterRepository = medicalMasterRepository;
        this.productMasterRepository = productMasterRepository;
        this.companyContext = companyContext;
        this.distributorRepository = distributorRepository;
        this.salesTransactionRepository = salesTransactionRepository;
        this.companyRepository = companyRepository;
        this.nigoSalesTransactionRepository = nigoSalesTransactionRepository;
        this.doctorWalletService = doctorWalletService;
    }


    private void applyCommissionMapping(SalesTransaction tx, CommissionMaster matchedCommission, Company company) {
        
        if (matchedCommission != null) {
            tx.setMedical(matchedCommission.getMedical());
            tx.setProduct(matchedCommission.getProduct());
            tx.setDoctor(matchedCommission.getDoctor());
            tx.setIsMatched(true);

            // Recalculate commission percent and amount
            java.math.BigDecimal commissionPercent = java.math.BigDecimal.ZERO;
            java.math.BigDecimal commissionAmount = java.math.BigDecimal.ZERO;
            if (matchedCommission.getCommissionPercentage() != null
                    && matchedCommission.getCommissionPercentage().compareTo(java.math.BigDecimal.ZERO) > 0) {
                commissionPercent = matchedCommission.getCommissionPercentage();
            } else if (tx.getProduct() != null && tx.getProduct().getDefaultCommissionPercentage() != null
                    && tx.getProduct().getDefaultCommissionPercentage().compareTo(java.math.BigDecimal.ZERO) > 0) {
                commissionPercent = tx.getProduct().getDefaultCommissionPercentage();
            }
            try {
                if (tx.getAmount() != null && !tx.getAmount().isEmpty() && commissionPercent != null) {
                    java.math.BigDecimal amt = new java.math.BigDecimal(tx.getAmount());
                    commissionAmount = amt.multiply(commissionPercent).divide(new java.math.BigDecimal("100"));
                }
            } catch (Exception e) {
                commissionAmount = java.math.BigDecimal.ZERO;
            }
            tx.setCommissionPercent(commissionPercent);
            tx.setCommissionAmount(commissionAmount);
        } else {
            // Set Medical and Product from raw names if possible
            MedicalMaster medical = null;
            ProductMaster product = null;
            if (tx.getRawMedicalName() != null && company != null) {
                List<MedicalMaster> medicals = medicalMasterRepository.findByNameIgnoreCaseAndCompanyIdAndIsActiveTrue(
                        tx.getRawMedicalName().trim(), company.getCompanyId()
                );
                if (medicals != null && !medicals.isEmpty()) {
                    medical = medicals.get(0);
                }
            }
            if (tx.getRawProductName() != null && company != null) {
                List<ProductMaster> products = productMasterRepository.findByNameIgnoreCaseAndCompanyIdAndIsActiveTrue(
                        tx.getRawProductName().trim(), company.getCompanyId()
                );
                if (products != null && !products.isEmpty()) {
                    product = products.get(0);
                }
            }
            tx.setMedical(medical);
            tx.setProduct(product);
            tx.setIsMatched(false);

            // Calculate commission percent and amount using product's default commission percentage if available
            java.math.BigDecimal commissionPercent = java.math.BigDecimal.ZERO;
            java.math.BigDecimal commissionAmount = java.math.BigDecimal.ZERO;
            if (product != null && product.getDefaultCommissionPercentage() != null
                    && product.getDefaultCommissionPercentage().compareTo(java.math.BigDecimal.ZERO) > 0) {
                commissionPercent = product.getDefaultCommissionPercentage();
            }
            try {
                if (tx.getAmount() != null && !tx.getAmount().isEmpty() && commissionPercent != null) {
                    java.math.BigDecimal amt = new java.math.BigDecimal(tx.getAmount());
                    commissionAmount = amt.multiply(commissionPercent).divide(new java.math.BigDecimal("100"));
                }
            } catch (Exception e) {
                commissionAmount = java.math.BigDecimal.ZERO;
            }
            tx.setCommissionPercent(commissionPercent);
            tx.setCommissionAmount(commissionAmount);
        }
    }

    public boolean rematchAndUpdateSalesTransaction(SalesTransaction tx, CommissionMaster matchedCommission) {
        if (tx == null || tx.getIsMatched() == null || tx.getIsMatched()) return false;
        log.info("rematchAndUpdateSalesTransaction - txId={}, rawMedical='{}', rawProduct='{}'",
                tx.getSalesTransactionId(), tx.getRawMedicalName(), tx.getRawProductName());
        applyCommissionMapping(tx, matchedCommission, tx.getCompany());
        if (tx.getIsMatched()) {
            salesTransactionRepository.save(tx);
            doctorWalletService.syncDoctorWalletLedger(tx, tx.getCreatedBy());
            log.info("rematchAndUpdateSalesTransaction - txId={} matched and saved", tx.getSalesTransactionId());
            return true;
        }
        log.debug("rematchAndUpdateSalesTransaction - txId={} could not be matched", tx.getSalesTransactionId());
        return false;
    }

    public com.org.app.dcas.dto.RematchResult rematchAndUpdateSalesTransactions(List<Long> salesTransactionIds) {
        log.info("rematchAndUpdateSalesTransactions - attempting to rematch {} transactions", salesTransactionIds.size());
        List<SalesTransaction> transactions = salesTransactionRepository.findAllById(salesTransactionIds);
        List<Long> successIds = new java.util.ArrayList<>();
        List<Long> failedIds = new java.util.ArrayList<>();

        Long companyId = companyContext.getCompanyId();
        List<CommissionMaster> commissions = commissionMasterRepository.findByCompanyId(companyId);
        java.util.Map<String, CommissionMaster> commissionMap = commissions.stream()
            .filter(cm -> cm.getMedical() != null && cm.getProduct() != null
                && cm.getMedical().getName() != null && cm.getProduct().getName() != null)
            .collect(java.util.stream.Collectors.toMap(
                cm -> cm.getMedical().getName().trim().toLowerCase() + "_" + cm.getProduct().getName().trim().toLowerCase(),
                cm -> cm
            ));
        log.info("rematchAndUpdateSalesTransactions - commissionMap size={}", commissionMap.size());

        transactions.parallelStream()
            .filter(tx -> tx.getIsMatched() != null && !tx.getIsMatched())
            .forEach(tx -> {
                String rawMedicalName = tx.getRawMedicalName();
                String rawProductName = tx.getRawProductName();
                String key = (rawMedicalName != null ? rawMedicalName.trim().toLowerCase() : "") + "_" +
                     (rawProductName != null ? rawProductName.trim().toLowerCase() : "");
                CommissionMaster matchedCommission = commissionMap.get(key);

                boolean updated = rematchAndUpdateSalesTransaction(tx, matchedCommission);
                if (updated) {
                    successIds.add(tx.getSalesTransactionId());
                } else {
                    failedIds.add(tx.getSalesTransactionId());
                }
            });
        log.info("rematchAndUpdateSalesTransactions - success={}, failed={}", successIds.size(), failedIds.size());
        return new com.org.app.dcas.dto.RematchResult(successIds, failedIds);
    }

    public static class Metrics {
        public int successCount = 0;
        public int nigoCount = 0;
        public int unmatchedCount = 0;
        public List<String> errors = new java.util.ArrayList<>();
    }

    private boolean checkDuplicate(SalesTransaction tx, Company company) {
        if (tx.getIsMatched()) {
            MedicalMaster medical = tx.getMedical();
            ProductMaster product = tx.getProduct();
            if (medical != null && product != null && tx.getVoucherNo() != null && tx.getFileAudit() != null) {
                return salesTransactionRepository
                    .existsByCompanyIdAndMedicalIdAndProductIdAndVoucherNoAndFileAuditIdNot(
                        company.getCompanyId(), medical.getMedicalId(), product.getProductId(), tx.getVoucherNo(), tx.getFileAudit().getFileAuditId());
            }
        } else {
            if (tx.getCompany() != null && tx.getRawMedicalName() != null && tx.getRawProductName() != null
                && tx.getVoucherNo() != null && tx.getFileAudit() != null) {
                return salesTransactionRepository
                    .existsByCompanyIdAndRawMedicalNameAndRawProductNameAndVoucherNoAndFileAuditIdNot(
                        company.getCompanyId(), tx.getRawMedicalName(), tx.getRawProductName(), tx.getVoucherNo(), tx.getFileAudit().getFileAuditId());
            }
        }
        return false;
    }

    private void saveNigoTransactionFromSalesTx(SalesTransaction tx, String reason) {
        NigoSalesTransaction nigoTx = new NigoSalesTransaction();
        nigoTx.setDistributor(tx.getDistributor());
        nigoTx.setCompany(tx.getCompany());
        nigoTx.setFileAudit(tx.getFileAudit());
        nigoTx.setRawMedicalName(tx.getRawMedicalName());
        nigoTx.setRawProductName(tx.getRawProductName());
        nigoTx.setMatched(tx.getIsMatched());
        nigoTx.setMedical(tx.getMedical());
        nigoTx.setProduct(tx.getProduct());
        nigoTx.setDoctor(tx.getDoctor());
        nigoTx.setVoucherNo(tx.getVoucherNo());
        nigoTx.setDate(tx.getDate());
        nigoTx.setQty(tx.getQty());
        nigoTx.setAmount(tx.getAmount());
        nigoTx.setCommissionPercent(tx.getCommissionPercent());
        nigoTx.setCommissionAmount(tx.getCommissionAmount());
        nigoTx.setCreatedBy(tx.getCreatedBy());
        nigoTx.setUpdatedBy(tx.getUpdatedBy());
        nigoTx.setReason(reason);
        nigoSalesTransactionRepository.save(nigoTx);
    }

    public void calculateAndPersistCommissionsWithMetrics(List<SalesExcelRow> salesRows, Long distributorId, FileAudit fileAudit, Metrics metrics) {

        Long companyId = companyContext.getCompanyId();
        log.info("calculateAndPersistCommissions - distributorId={}, companyId={}, rowCount={}",
                distributorId, companyId, salesRows.size());

        List<CommissionMaster> commissions = commissionMasterRepository.findByCompanyId(companyId);
        java.util.Map<String, CommissionMaster> commissionMap = commissions.stream()
            .filter(cm -> cm.getMedical() != null && cm.getProduct() != null
                && cm.getMedical().getName() != null && cm.getProduct().getName() != null)
            .collect(java.util.stream.Collectors.toMap(
                cm -> cm.getMedical().getName().trim().toLowerCase() + "_" + cm.getProduct().getName().trim().toLowerCase(),
                cm -> cm
            ));
        log.info("calculateAndPersistCommissions - commissionMap size={} for companyId={}", commissionMap.size(), companyId);

        Distributor distributor = distributorRepository.findById(distributorId)
                .orElseThrow(() -> new IllegalArgumentException("Distributor not found for id: " + distributorId));
        Company company = companyRepository.findById(companyContext.getCompanyId()).orElse(null);
        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : null;

        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger nigoCount = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger unmatchedCount = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.ConcurrentLinkedQueue<String> errors = new java.util.concurrent.ConcurrentLinkedQueue<>();

        salesRows.parallelStream().forEach(row -> {
            try {

                String rawMedicalName = row.getMedical();
                String rawProductName = row.getProductName();
                String key = (rawMedicalName != null ? rawMedicalName.trim().toLowerCase() : "") + "_" +
                     (rawProductName != null ? rawProductName.trim().toLowerCase() : "");
                CommissionMaster matchedCommission = commissionMap.get(key);

                SalesTransaction tx = new SalesTransaction();
                // ...set distributor, company, fileAudit, rawMedicalName, rawProductName, voucherNo, date, qty, amount, createdBy, updatedBy...
                tx.setDistributor(distributor);
                tx.setCompany(company);
                tx.setFileAudit(fileAudit);
                tx.setRawMedicalName(row.getMedical());
                tx.setRawProductName(row.getProductName());
                tx.setVoucherNo(row.getVoucherNo());
                tx.setDate(row.getDate());
                tx.setQty(row.getQty() != null ? row.getQty().toString() : null);
                tx.setAmount(row.getAmount() != null ? row.getAmount().toString() : null);
                tx.setCreatedBy(userIdStr);
                tx.setUpdatedBy(userIdStr);

                applyCommissionMapping(tx, matchedCommission, company);

                boolean duplicateExists = checkDuplicate(tx, company);

                if (duplicateExists) {
                    saveNigoTransactionFromSalesTx(tx, "Duplicate transaction for MedicalId, ProductId, VoucherNo");
                    nigoCount.incrementAndGet();
                } else {
                    try {
                        SalesTransaction savedTx = salesTransactionRepository.save(tx);
                        doctorWalletService.syncDoctorWalletLedger(savedTx, userIdStr);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        errors.add("Error saving SalesTransaction for row: " + row + " - " + e.getMessage());
                    }
                }

                if (!tx.getIsMatched()) {
                    unmatchedCount.incrementAndGet();
                }
            } catch (Exception e) {
                errors.add("General error for row: " + row + " - " + e.getMessage());
            }
        });

        metrics.successCount = successCount.get();
        metrics.nigoCount = nigoCount.get();
        metrics.unmatchedCount = unmatchedCount.get();
        metrics.errors = new java.util.ArrayList<>(errors);
        log.info("calculateAndPersistCommissions - done: success={}, nigo={}, unmatched={}, errors={}",
                metrics.successCount, metrics.nigoCount, metrics.unmatchedCount, metrics.errors.size());
    }
}