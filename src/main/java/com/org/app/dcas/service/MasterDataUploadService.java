package com.org.app.dcas.service;

import com.org.app.dcas.model.DoctorMaster;
import com.org.app.dcas.model.CommissionMaster;
import com.org.app.dcas.model.ProductMaster;
import com.org.app.dcas.model.Distributor;
import com.org.app.dcas.model.MedicalMaster;
import com.org.app.dcas.repository.DoctorMasterRepository;
import com.org.app.dcas.repository.CommissionMasterRepository;
import com.org.app.dcas.repository.ProductMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import com.org.app.dcas.repository.DistributorRepository;
import com.org.app.dcas.repository.MedicalMasterRepository;
import com.org.app.dcas.context.CompanyContext;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class MasterDataUploadService {

    private static final Logger log = LoggerFactory.getLogger(MasterDataUploadService.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DistributorRepository distributorRepository;

    @Autowired
    private DoctorMasterRepository doctorMasterRepository;

    @Autowired
    private CommissionMasterRepository commissionMasterRepository;

    @Autowired
    private ProductMasterRepository productMasterRepository;

    @Autowired
    private MedicalMasterRepository medicalMasterRepository;

    @Autowired
    private CompanyContext companyContext;

    @Transactional
    public Map<String, Object> processMasterDataExcel(MultipartFile file) {
        log.info("processMasterDataExcel - starting upload");
        Map<String, Object> response = new LinkedHashMap<>();
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);

            Long companyId = null;
            Long distributorId = null;

            // 1. Company — always use the logged-in user's company from context.
            // Never create or look up a company from the Excel file to avoid
            // creating orphan companies that are not linked to the current user.
            companyId = companyContext.getCompanyId();
            if (companyId == null) {
                response.put("status", "error");
                response.put("errorMessage", "No company context found for current user");
                return response;
            }
            log.info("processMasterDataExcel - using companyId={} from context", companyId);

            // 2. Distributor
            Sheet distributorSheet = workbook.getSheet("distributor");
            if (distributorSheet != null && companyId != null) {
                for (Row row : distributorSheet) {
                    if (row.getRowNum() == 0) continue;
                    String distributorName = getString(row.getCell(0));
                    //String distributorAddress = getString(row.getCell(1));
                    String distributorPhone = getString(row.getCell(2));
                    String distributorEmail = getString(row.getCell(3));
                    if (distributorName == null || distributorName.trim().isEmpty()) continue;
                    Distributor distributor = distributorRepository.findByDistributorNameAndCompanyId(distributorName, companyId).orElse(null);
                    if (distributor == null) {
                        distributor = new Distributor();
                        distributor.setDistributorName(distributorName);
                        distributor.setCompany(companyRepository.findById(companyId).orElse(null));
                        distributor.setPhoneNumber(distributorPhone);
                        distributor.setEmail(distributorEmail);
                        // Set createdBy/updatedBy from context
                        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
                        distributor.setCreatedBy(userIdStr);
                        distributor.setUpdatedBy(userIdStr);
                        distributor = distributorRepository.save(distributor);
                    }
                    distributorId = distributor.getDistributorId();
                }
            }

            int totalDoctors = 0;
            int totalProducts = 0;
            int totalMedicals = 0;
            int totalCommissions = 0;
            int updatedDoctors = 0;
            int updatedProducts = 0;
            int updatedMedicals = 0;
            int updatedCommissions = 0;

            Map<String, DoctorMaster> doctorMap = new HashMap<>();
            Map<String, ProductMaster> productMap = new HashMap<>();
            Map<String, MedicalMaster> medicalMap = new HashMap<>();

            // 3. Doctor
            Sheet doctorSheet = workbook.getSheet("doctor_master");
            if (doctorSheet != null && companyId != null) {
                for (Row row : doctorSheet) {
                    if (row.getRowNum() == 0) continue;
                    String doctorName = getString(row.getCell(0));
                    if (doctorName == null || doctorName.trim().isEmpty()) continue;
                    if (doctorMap.containsKey(doctorName)) continue; // skip within-file duplicates
                    String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
                    java.util.Optional<DoctorMaster> existing = doctorMasterRepository.findByNameIgnoreCaseAndCompanyIdAndIsActiveTrue(doctorName.trim(), companyId);
                    DoctorMaster doctor;
                    if (existing.isPresent()) {
                        doctor = existing.get();
                        doctor.setSpecialization(getString(row.getCell(1)));
                        doctor.setPhoneNumber(getString(row.getCell(2)));
                        doctor.setEmail(getString(row.getCell(3)));
                        doctor.setUpdatedBy(userIdStr);
                        updatedDoctors++;
                    } else {
                        doctor = new DoctorMaster();
                        doctor.setName(doctorName);
                        doctor.setSpecialization(getString(row.getCell(1)));
                        doctor.setPhoneNumber(getString(row.getCell(2)));
                        doctor.setEmail(getString(row.getCell(3)));
                        doctor.setCompany(companyRepository.findById(companyId).orElse(null));
                        doctor.setCreatedBy(userIdStr);
                        doctor.setUpdatedBy(userIdStr);
                        totalDoctors++;
                    }
                    DoctorMaster savedDoctor = doctorMasterRepository.save(doctor);
                    doctorMap.put(savedDoctor.getName().trim().toLowerCase(), savedDoctor);
                }
            }

            // 4. Product
            Sheet productSheet = workbook.getSheet("product_master");
            if (productSheet != null && companyId != null) {
                for (Row row : productSheet) {
                    if (row.getRowNum() == 0) continue;
                    String productName = getString(row.getCell(0));
                    if (productName == null || productName.trim().isEmpty()) continue;
                    if (productMap.containsKey(productName)) continue; // skip within-file duplicates
                    String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
                    java.util.List<ProductMaster> existingList = productMasterRepository.findByNameIgnoreCaseAndCompanyIdAndIsActiveTrue(productName.trim(), companyId);
                    ProductMaster product;
                    if (!existingList.isEmpty()) {
                        product = existingList.get(0);
                        product.setDescription(getString(row.getCell(1)));
                        product.setDefaultCommissionPercentage(getDouble(row.getCell(2)) != null ? java.math.BigDecimal.valueOf(getDouble(row.getCell(2))) : product.getDefaultCommissionPercentage());
                        product.setUpdatedBy(userIdStr);
                        updatedProducts++;
                    } else {
                        product = new ProductMaster();
                        product.setName(productName);
                        product.setDescription(getString(row.getCell(1)));
                        product.setDefaultCommissionPercentage(getDouble(row.getCell(2)) != null ? java.math.BigDecimal.valueOf(getDouble(row.getCell(2))) : java.math.BigDecimal.ZERO);
                        product.setCompany(companyRepository.findById(companyId).orElse(null));
                        product.setCreatedBy(userIdStr);
                        product.setUpdatedBy(userIdStr);
                        totalProducts++;
                    }
                    ProductMaster savedProduct = productMasterRepository.save(product);
                    productMap.put(savedProduct.getName().trim().toLowerCase(), savedProduct);
                }
            }

            // 5. Medical
            Sheet medicalSheet = workbook.getSheet("medical_master");
            if (medicalSheet != null && companyId != null) {
                for (Row row : medicalSheet) {
                    if (row.getRowNum() == 0) continue;
                    String medicalName = getString(row.getCell(0));
                    if (medicalName == null || medicalName.trim().isEmpty()) continue;
                    if (medicalMap.containsKey(medicalName)) continue; // skip within-file duplicates
                    String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
                    java.util.List<MedicalMaster> existingList = medicalMasterRepository.findByNameIgnoreCaseAndCompanyIdAndIsActiveTrue(medicalName.trim(), companyId);
                    MedicalMaster medical;
                    if (!existingList.isEmpty()) {
                        medical = existingList.get(0);
                        medical.setAddress(getString(row.getCell(1)));
                        medical.setPhoneNumber(getString(row.getCell(2)));
                        medical.setEmail(getString(row.getCell(3)));
                        medical.setUpdatedBy(userIdStr);
                        updatedMedicals++;
                    } else {
                        medical = new MedicalMaster();
                        medical.setName(medicalName);
                        medical.setAddress(getString(row.getCell(1)));
                        medical.setPhoneNumber(getString(row.getCell(2)));
                        medical.setEmail(getString(row.getCell(3)));
                        medical.setCompany(companyRepository.findById(companyId).orElse(null));
                        medical.setCreatedBy(userIdStr);
                        medical.setUpdatedBy(userIdStr);
                        totalMedicals++;
                    }
                    MedicalMaster savedMedical = medicalMasterRepository.save(medical);
                    medicalMap.put(savedMedical.getName().trim().toLowerCase(), savedMedical);
                }
            }

            // 6. Commission
            Sheet commissionSheet = workbook.getSheet("commission_master");
            if (commissionSheet != null && companyId != null) {
                for (Row row : commissionSheet) {
                    if (row.getRowNum() == 0) continue;
                    String medicalName = getString(row.getCell(0));
                    String productName = getString(row.getCell(1));
                    String doctorName = getString(row.getCell(2));
                    Double commissionPercent = getDouble(row.getCell(3));
                    if (doctorName == null || doctorName.trim().isEmpty() ||
                        medicalName == null || medicalName.trim().isEmpty() ||
                        productName == null || productName.trim().isEmpty()) continue;

                    DoctorMaster doctor = doctorMap.get(doctorName.trim().toLowerCase());
                    MedicalMaster medical = medicalMap.get(medicalName.trim().toLowerCase());
                    ProductMaster product = productMap.get(productName.trim().toLowerCase());

                    if (doctor != null && medical != null && product != null) {
                        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
                        java.util.Optional<CommissionMaster> existingCommission =
                            commissionMasterRepository.findByDoctorDoctorIdAndMedicalMedicalIdAndProductProductIdAndCompanyId(
                                doctor.getDoctorId(), medical.getMedicalId(), product.getProductId(), companyId);
                        CommissionMaster commission;
                        if (existingCommission.isPresent()) {
                            commission = existingCommission.get();
                            commission.setCommissionPercentage(commissionPercent != null ? java.math.BigDecimal.valueOf(commissionPercent) : commission.getCommissionPercentage());
                            commission.setUpdatedBy(userIdStr);
                            updatedCommissions++;
                        } else {
                            commission = new CommissionMaster();
                            commission.setDoctor(doctor);
                            commission.setMedical(medical);
                            commission.setProduct(product);
                            commission.setCommissionPercentage(commissionPercent != null ? java.math.BigDecimal.valueOf(commissionPercent) : java.math.BigDecimal.ZERO);
                            commission.setCompany(companyRepository.findById(companyId).orElse(null));
                            commission.setCreatedBy(userIdStr);
                            commission.setUpdatedBy(userIdStr);
                            totalCommissions++;
                        }
                        commissionMasterRepository.save(commission);
                    }
                }
            }

            // At the end, instead of returning a string, populate the response map
            response.put("companyId", companyId);
            response.put("distributorId", distributorId);
            response.put("doctorsCreated", totalDoctors);
            response.put("doctorsUpdated", updatedDoctors);
            response.put("productsCreated", totalProducts);
            response.put("productsUpdated", updatedProducts);
            response.put("medicalsCreated", totalMedicals);
            response.put("medicalsUpdated", updatedMedicals);
            response.put("commissionsCreated", totalCommissions);
            response.put("commissionsUpdated", updatedCommissions);
            response.put("status", "success");
            log.info("processMasterDataExcel - done: companyId={}, distributorId={}, doctors={}, products={}, medicals={}, commissions={}",
                    companyId, distributorId, totalDoctors, totalProducts, totalMedicals, totalCommissions);
        } catch (Exception e) {
            log.error("processMasterDataExcel - error: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("errorMessage", e.getMessage());
        }
        return response;
    }

    // Utility methods for safe cell value extraction
    private String getString(Cell cell) {
        return cell == null ? null : cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : cell.toString();
    }

    private Double getDouble(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        try { return Double.parseDouble(cell.toString()); } catch (Exception e) { return null; }
    }
}
