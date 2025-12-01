package com.org.app.dcas.service;

import com.org.app.dcas.model.*;
import com.org.app.dcas.dto.*;
import com.org.app.dcas.repository.*;
import com.org.app.dcas.context.CompanyContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.org.app.dcas.model.FileAudit;
import com.org.app.dcas.repository.FileAuditRepository;

@Service
public class CommissionService {

    private final CompanyRepository companyRepository;
    private final MedicalMasterRepository medicalRepository;
    private final ProductMasterRepository productRepository;
    private final DoctorMasterRepository doctorMasterRepository;
    private final CommissionMasterRepository commissionRepository;
    private final CompanyContext companyContext;
    private final CommissionCalculator commissionCalculator;
    private final FileAuditRepository fileAuditRepository;

    public CommissionService(CompanyRepository companyRepository,
                             MedicalMasterRepository medicalRepository,
                             ProductMasterRepository productRepository,
                             DoctorMasterRepository doctorMasterRepository,
                             CommissionMasterRepository commissionRepository,
                             CompanyContext companyContext,
                             CommissionCalculator commissionCalculator,
                             FileAuditRepository fileAuditRepository) {
        this.companyRepository = companyRepository;
        this.medicalRepository = medicalRepository;
        this.productRepository = productRepository;
        this.doctorMasterRepository = doctorMasterRepository;
        this.commissionRepository = commissionRepository;
        this.companyContext = companyContext;
        this.commissionCalculator = commissionCalculator;
        this.fileAuditRepository = fileAuditRepository;
    }

    @Transactional
    public DoctorResponse createDoctorWithProducts(DoctorWithProductsRequest req) {
        DoctorMaster d = new DoctorMaster();
        d.setName(req.getName());
        d.setSpecialization(req.getSpecialization());
        d.setPhoneNumber(req.getPhoneNumber());
        d.setEmail(req.getEmail());

        Long companyId = companyContext.getCompanyId();
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required (from context)");
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid companyId"));
        d.setCompany(company);

        // Set createdBy/updatedBy from context
        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
        d.setCreatedBy(userIdStr);
        d.setUpdatedBy(userIdStr);

        DoctorMaster savedDoctor = doctorMasterRepository.save(d);

        final MedicalMaster medicalForCommission = (req.getMedicalId() != null)
                ? medicalRepository.findById(req.getMedicalId()).orElse(null)
                : null;

        List<ProductCommissionResponse> commissionResponses = new ArrayList<>();

        if (req.getProductCommissions() != null) {
            for (ProductCommission pc : req.getProductCommissions()) {
                if (pc == null || pc.getProductId() == null) continue;
                productRepository.findById(pc.getProductId()).ifPresent(p -> {
                    CommissionMaster c = new CommissionMaster();
                    c.setDoctor(savedDoctor);
                    c.setProduct(p);
                    c.setMedical(medicalForCommission);
                    c.setCompany(company);
                    c.setCommissionPercentage(pc.getCommissionPercentage() != null ? pc.getCommissionPercentage() : BigDecimal.ZERO);
                    // Set createdBy/updatedBy for commission
                    c.setCreatedBy(userIdStr);
                    c.setUpdatedBy(userIdStr);
                    commissionRepository.save(c);

                    commissionResponses.add(new ProductCommissionResponse(p, c.getCommissionPercentage()));
                });
            }
        }

        return new DoctorResponse(savedDoctor, medicalForCommission, commissionResponses);
    }

    // CRUD methods for commission_master

    public List<CommissionMaster> getAllCommissionsForCurrentCompany() {
        return commissionRepository.findByCompanyId(companyContext.getCompanyId());
    }

    public List<CommissionResponse> getActiveCommissionsForCurrentCompany() {
        List<CommissionMaster> masters = commissionRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
        List<CommissionResponse> responses = new ArrayList<>();
        for (CommissionMaster cm : masters) {
            responses.add(CommissionResponse.from(cm));
        }
        return responses;
    }

    public CommissionResponse getCommissionByIdForCurrentCompany(Long commissionId) {
        CommissionMaster cm = commissionRepository.findByCommissionIdAndCompanyId(commissionId, companyContext.getCompanyId()).orElse(null);
        if (cm == null) return null;
        return CommissionResponse.from(cm);
    }

    public CommissionMaster createCommissionForCurrentCompany(CommissionMaster commission) {
        Long companyId = companyContext.getCompanyId();
        Company company = companyRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id"));
        commission.setCompany(company);

        // Set createdBy/updatedBy from context
        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
        commission.setCreatedBy(userIdStr);
        commission.setUpdatedBy(userIdStr);

        // Ensure doctor, product, medical are set from their respective masters
        if (commission.getDoctor() != null && commission.getDoctor().getDoctorId() != null) {
            DoctorMaster doctor = doctorMasterRepository.findById(commission.getDoctor().getDoctorId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid doctor id"));
            commission.setDoctor(doctor);
        }
        if (commission.getProduct() != null && commission.getProduct().getProductId() != null) {
            ProductMaster product = productRepository.findById(commission.getProduct().getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product id"));
            commission.setProduct(product);
        }
        if (commission.getMedical() != null && commission.getMedical().getMedicalId() != null) {
            MedicalMaster medical = medicalRepository.findById(commission.getMedical().getMedicalId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid medical id"));
            commission.setMedical(medical);
        }

        return commissionRepository.save(commission);
    }

    public CommissionMaster updateCommissionForCurrentCompany(Long commissionId, CommissionMaster commission) {
        return commissionRepository.findByCommissionIdAndCompanyId(commissionId, companyContext.getCompanyId()).map(existing -> {
            // Update only allowed fields
            if (commission.getDoctor() != null && commission.getDoctor().getDoctorId() != null) {
                DoctorMaster doctor = doctorMasterRepository.findById(commission.getDoctor().getDoctorId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid doctor id"));
                existing.setDoctor(doctor);
            }
            if (commission.getProduct() != null && commission.getProduct().getProductId() != null) {
                ProductMaster product = productRepository.findById(commission.getProduct().getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid product id"));
                existing.setProduct(product);
            }
            if (commission.getMedical() != null && commission.getMedical().getMedicalId() != null) {
                MedicalMaster medical = medicalRepository.findById(commission.getMedical().getMedicalId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid medical id"));
                existing.setMedical(medical);
            }
            if (commission.getCommissionPercentage() != null) {
                existing.setCommissionPercentage(commission.getCommissionPercentage());
            }
            // Set updatedBy from context
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            return commissionRepository.save(existing);
        }).orElse(null);
    }

    public boolean softDeleteCommissionForCurrentCompany(Long commissionId) {
        return commissionRepository.findByCommissionIdAndCompanyId(commissionId, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            // Set updatedBy from context
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            commissionRepository.save(existing);
            return true;
        }).orElse(false);
    }


    public FileAudit processSalesExcelWithMetrics(MultipartFile file, Long distributorId, Long userId, String fileName) {
        FileAudit fileAudit = new FileAudit();
        fileAudit.setFileName(fileName);
        fileAudit.setUploadedBy(userId);
        fileAudit.setUploadedAt(java.time.LocalDateTime.now());
        // Save FileAudit before passing to commissionCalculator
        fileAudit = fileAuditRepository.save(fileAudit);

        processSalesExcelInternalWithMetrics(file, distributorId, fileAudit);

        fileAuditRepository.save(fileAudit);
        return fileAudit;
    }


    private void processSalesExcelInternalWithMetrics(MultipartFile file, Long distributorId, FileAudit fileAudit) {
        List<SalesExcelRow> salesRows = new ArrayList<>();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

        int totalRows = 0;
        int successCount = 0;
        int nigoCount = 0;
        int unmatchedCount = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheet("Pharmario Main");
            if (sheet == null) sheet = workbook.getSheetAt(0);
            if (sheet == null) throw new IllegalArgumentException("No sheet found in Excel file");

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new IllegalArgumentException("Header row not found in Excel sheet");

            int dateIdx = -1, voucherIdx = -1, medicalIdx = -1, productIdx = -1, qtyIdx = -1, doctorIdx = -1, amountIdx = -1;
            for (Cell cell : headerRow) {
                String val = cell.getStringCellValue().trim();
                if ("Date".equalsIgnoreCase(val)) dateIdx = cell.getColumnIndex();
                else if ("Voucher no".equalsIgnoreCase(val)) voucherIdx = cell.getColumnIndex();
                else if ("MEDICAL".equalsIgnoreCase(val)) medicalIdx = cell.getColumnIndex();
                else if ("Product Name".equalsIgnoreCase(val)) productIdx = cell.getColumnIndex();
                else if ("Qty".equalsIgnoreCase(val)) qtyIdx = cell.getColumnIndex();
                else if ("Doctor".equalsIgnoreCase(val)) doctorIdx = cell.getColumnIndex();
                else if ("Amount".equalsIgnoreCase(val)) amountIdx = cell.getColumnIndex();
            }

            if (dateIdx == -1 || voucherIdx == -1 || medicalIdx == -1 || productIdx == -1 ||
                qtyIdx == -1 || doctorIdx == -1 || amountIdx == -1) {
                throw new IllegalArgumentException("One or more required columns not found");
            }

            Iterator<Row> rowIterator = sheet.rowIterator();
            if (rowIterator.hasNext()) rowIterator.next(); // skip header

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                SalesExcelRow salesRow = new SalesExcelRow();
                salesRow.setDistributorId(distributorId);

                String dateStr = getCellString(row.getCell(dateIdx));
                LocalDate localDate = null;
                try {
                    localDate = LocalDate.parse(dateStr, formatter);
                } catch (Exception e) {
                    if (dateStr != null && dateStr.contains("T")) {
                        localDate = LocalDate.parse(dateStr.substring(0, dateStr.indexOf("T")));
                    }
                }
                salesRow.setDate(localDate);

                salesRow.setVoucherNo(getCellString(row.getCell(voucherIdx)));
                salesRow.setMedical(getCellString(row.getCell(medicalIdx)));
                salesRow.setProductName(getCellString(row.getCell(productIdx)));
                String qtyStr = getCellString(row.getCell(qtyIdx));
                try {
                    salesRow.setQty(qtyStr != null && !qtyStr.isEmpty() ? Integer.valueOf(qtyStr) : null);
                } catch (Exception e) {
                    salesRow.setQty(null);
                }
                //salesRow.setDoctor(getCellString(row.getCell(doctorIdx)));
                String amtStr = getCellString(row.getCell(amountIdx));
                try {
                    if (row.getCell(amountIdx) != null && row.getCell(amountIdx).getCellType() == CellType.FORMULA) {
                        double val = row.getCell(amountIdx).getNumericCellValue();
                        salesRow.setAmount(val);
                    } else {
                        salesRow.setAmount(amtStr != null && !amtStr.isEmpty() ? Double.valueOf(amtStr) : null);
                    }
                } catch (Exception e) {
                    errors.add("Invalid amount value: " + amtStr + " at row " + row.getRowNum());
                    salesRow.setAmount(null);
                }
                salesRows.add(salesRow);
            }
            workbook.close();

            totalRows = salesRows.size();

            // Track metrics during commission calculation
            CommissionCalculator.Metrics metrics = new CommissionCalculator.Metrics();
            commissionCalculator.calculateAndPersistCommissionsWithMetrics(salesRows, distributorId, fileAudit, metrics);

            successCount = metrics.successCount;
            nigoCount = metrics.nigoCount;
            unmatchedCount = metrics.unmatchedCount;
            errors.addAll(metrics.errors);

            // Set metrics in fileAudit from response
            fileAudit.setTotalRows(totalRows);
            fileAudit.setSuccessCount(successCount);
            fileAudit.setNigoCount(nigoCount);
            fileAudit.setUnmatchedCount(unmatchedCount);
            fileAudit.setErrorCount(errors.size());
            fileAudit.setErrors(errors);

        } catch (Exception e) {
            errors.add("Error processing Excel: " + e.getMessage());
            fileAudit.setErrors(errors);
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    double val = cell.getNumericCellValue();
                    if (val == (long) val) {
                        return String.valueOf((long) val);
                    } else {
                        return String.valueOf(val);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
