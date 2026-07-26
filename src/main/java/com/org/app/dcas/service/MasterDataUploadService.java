package com.org.app.dcas.service;

import com.org.app.dcas.model.DoctorMaster;
import com.org.app.dcas.model.CommissionMaster;
import com.org.app.dcas.model.ProductMaster;
import com.org.app.dcas.model.Company;
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

            // 1. Company
            Sheet companySheet = workbook.getSheet("company");
            if (companySheet != null) {
                for (Row row : companySheet) {
                    if (row.getRowNum() == 0) continue;
                    String companyName = getString(row.getCell(0));
                    String companyAddress = getString(row.getCell(1));
                    String companyPhone = getString(row.getCell(2));
                    String companyEmail = getString(row.getCell(3));
                    if (companyName == null || companyName.trim().isEmpty()) continue;
                    Company company = companyRepository.findByName(companyName).orElse(null);
                    if (company == null) {
                        company = new Company();
                        company.setName(companyName);
                        company.setAddress(companyAddress);
                        company.setPhoneNumber(companyPhone);
                        company.setEmail(companyEmail);
                        // Set createdBy/updatedBy from context
                        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
                        company.setCreatedBy(userIdStr);
                        company.setUpdatedBy(userIdStr);
                        company = companyRepository.save(company);
                    }
                    companyId = company.getCompanyId();
                }
            }

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
                    if (doctorMap.containsKey(doctorName)) {
                        throw new RuntimeException("Duplicate doctor name found: " + doctorName);
                    }
                    DoctorMaster doctor = new DoctorMaster();
                    doctor.setName(doctorName);
                    doctor.setSpecialization(getString(row.getCell(1)));
                    doctor.setPhoneNumber(getString(row.getCell(2)));
                    doctor.setEmail(getString(row.getCell(3)));
                    doctor.setCompany(companyRepository.findById(companyId).orElse(null));
                    // Set createdBy/updatedBy from context
                    String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
                    doctor.setCreatedBy(userIdStr);
                    doctor.setUpdatedBy(userIdStr);
                    //TODO : MR pending
                    // set other fields as needed
                    DoctorMaster savedDoctor = doctorMasterRepository.save(doctor);
                    doctorMap.put(savedDoctor.getName(), savedDoctor);
                    totalDoctors++;
                }
            }

            // 4. Product
            Sheet productSheet = workbook.getSheet("product_master");
            if (productSheet != null && companyId != null) {
                for (Row row : productSheet) {
                    if (row.getRowNum() == 0) continue;
                    String productName = getString(row.getCell(0));
                    if (productName == null || productName.trim().isEmpty()) continue;
                    if (productMap.containsKey(productName)) {
                        throw new RuntimeException("Duplicate product name found: " + productName);
                    }
                    ProductMaster product = new ProductMaster();
                    product.setName(productName);
                    product.setDescription(getString(row.getCell(1)));
                    product.setDefaultCommissionPercentage(getDouble(row.getCell(2)) != null ? java.math.BigDecimal.valueOf(getDouble(row.getCell(2))) : java.math.BigDecimal.ZERO);
                    product.setCompany(companyRepository.findById(companyId).orElse(null));
                    // Set createdBy/updatedBy from context
                    String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
                    product.setCreatedBy(userIdStr);
                    product.setUpdatedBy(userIdStr);
                    // set other fields as needed
                    ProductMaster savedProduct = productMasterRepository.save(product);
                    productMap.put(savedProduct.getName(), savedProduct);
                    totalProducts++;
                }
            }

            // 5. Medical
            Sheet medicalSheet = workbook.getSheet("medical_master");
            if (medicalSheet != null && companyId != null) {
                for (Row row : medicalSheet) {
                    if (row.getRowNum() == 0) continue;
                    String medicalName = getString(row.getCell(0));
                    if (medicalName == null || medicalName.trim().isEmpty()) continue;
                    if (medicalMap.containsKey(medicalName)) {
                        throw new RuntimeException("Duplicate medical name found: " + medicalName);
                    }
                    MedicalMaster medical = new MedicalMaster();
                    medical.setName(medicalName);
                    medical.setAddress(getString(row.getCell(1)));
                    medical.setPhoneNumber(getString(row.getCell(2)));
                    medical.setEmail(getString(row.getCell(3)));
                    medical.setCompany(companyRepository.findById(companyId).orElse(null));
                    // Set createdBy/updatedBy from context
                    String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
                    medical.setCreatedBy(userIdStr);
                    medical.setUpdatedBy(userIdStr);
                    // set other fields as needed
                    MedicalMaster savedMedical = medicalMasterRepository.save(medical);
                    medicalMap.put(savedMedical.getName(), savedMedical);
                    totalMedicals++;
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

                    DoctorMaster doctor = doctorMap.get(doctorName);
                    MedicalMaster medical = medicalMap.get(medicalName);
                    ProductMaster product = productMap.get(productName);

                    if (doctor != null && medical != null && product != null) {
                        CommissionMaster commission = new CommissionMaster();
                        commission.setDoctor(doctor);
                        commission.setMedical(medical);
                        commission.setProduct(product);
                        commission.setCommissionPercentage(commissionPercent != null ? java.math.BigDecimal.valueOf(commissionPercent) : java.math.BigDecimal.ZERO);
                        commission.setCompany(companyRepository.findById(companyId).orElse(null));
                        // Set createdBy/updatedBy from context
                        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
                        commission.setCreatedBy(userIdStr);
                        commission.setUpdatedBy(userIdStr);
                        // set other fields as needed
                        commissionMasterRepository.save(commission);
                        totalCommissions++;
                    }
                }
            }

            // At the end, instead of returning a string, populate the response map
            response.put("companyId", companyId);
            response.put("distributorId", distributorId);
            response.put("doctorsInserted", totalDoctors);
            response.put("productsInserted", totalProducts);
            response.put("medicalsInserted", totalMedicals);
            response.put("commissionsInserted", totalCommissions);
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
