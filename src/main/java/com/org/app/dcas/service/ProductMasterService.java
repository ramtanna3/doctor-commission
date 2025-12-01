package com.org.app.dcas.service;

import com.org.app.dcas.model.ProductMaster;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.ProductMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductMasterService {

    private final ProductMasterRepository productMasterRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public ProductMasterService(ProductMasterRepository productMasterRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.productMasterRepository = productMasterRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<ProductMaster> getAllProductsForCurrentCompany() {
        return productMasterRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
    }

    public ProductMaster getProductByIdForCurrentCompany(Long productId) {
        return productMasterRepository.findByProductIdAndCompanyId(productId, companyContext.getCompanyId()).orElse(null);
    }

    public ProductMaster createProductForCurrentCompany(ProductMaster product) {
        Long companyId = companyContext.getCompanyId();
        product.setCompany(companyRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
        product.setCreatedBy(userIdStr);
        product.setUpdatedBy(userIdStr);
        return productMasterRepository.save(product);
    }

    public ProductMaster updateProductForCurrentCompany(Long productId, ProductMaster product) {
        return productMasterRepository.findByProductIdAndCompanyId(productId, companyContext.getCompanyId()).map(existing -> {
            existing.setName(product.getName());
            existing.setDescription(product.getDescription());
            existing.setDefaultCommissionPercentage(product.getDefaultCommissionPercentage());
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            return productMasterRepository.save(existing);
        }).orElse(null);
    }

    public boolean softDeleteProductForCurrentCompany(Long productId) {
        return productMasterRepository.findByProductIdAndCompanyId(productId, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            // Set updatedBy from context
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            productMasterRepository.save(existing);
            return true;
        }).orElse(false);
    }
}
