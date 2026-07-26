package com.org.app.dcas.service;

import com.org.app.dcas.model.ProductMaster;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.ProductMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductMasterService {

    private static final Logger log = LoggerFactory.getLogger(ProductMasterService.class);

    private final ProductMasterRepository productMasterRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public ProductMasterService(ProductMasterRepository productMasterRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.productMasterRepository = productMasterRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<ProductMaster> getAllProductsForCurrentCompany() {
        Long companyId = companyContext.getCompanyId();
        log.info("getAllProducts - companyId={}", companyId);
        List<ProductMaster> result = productMasterRepository.findByCompanyIdAndIsActiveTrue(companyId);
        log.info("getAllProducts - returned {} products", result.size());
        return result;
    }

    public ProductMaster getProductByIdForCurrentCompany(Long productId) {
        log.info("getProductById - productId={}", productId);
        ProductMaster result = productMasterRepository.findByProductIdAndCompanyId(productId, companyContext.getCompanyId()).orElse(null);
        if (result == null) log.warn("getProductById - productId={} not found", productId);
        return result;
    }

    public ProductMaster createProductForCurrentCompany(ProductMaster product) {
        Long companyId = companyContext.getCompanyId();
        log.info("createProduct - companyId={}, name='{}'", companyId, product.getName());
        product.setCompany(companyRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
        product.setCreatedBy(userIdStr);
        product.setUpdatedBy(userIdStr);
        ProductMaster saved = productMasterRepository.save(product);
        log.info("createProduct - created productId={}", saved.getProductId());
        return saved;
    }

    public ProductMaster updateProductForCurrentCompany(Long productId, ProductMaster product) {
        log.info("updateProduct - productId={}", productId);
        return productMasterRepository.findByProductIdAndCompanyId(productId, companyContext.getCompanyId()).map(existing -> {
            existing.setName(product.getName());
            existing.setDescription(product.getDescription());
            existing.setDefaultCommissionPercentage(product.getDefaultCommissionPercentage());
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            ProductMaster saved = productMasterRepository.save(existing);
            log.info("updateProduct - productId={} updated", productId);
            return saved;
        }).orElseGet(() -> { log.warn("updateProduct - productId={} not found", productId); return null; });
    }

    public boolean softDeleteProductForCurrentCompany(Long productId) {
        log.info("softDeleteProduct - productId={}", productId);
        return productMasterRepository.findByProductIdAndCompanyId(productId, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            String userIdStr = companyContext.getUserId() != null ? companyContext.getUserId().toString() : "system";
            existing.setUpdatedBy(userIdStr);
            productMasterRepository.save(existing);
            log.info("softDeleteProduct - productId={} deactivated", productId);
            return true;
        }).orElseGet(() -> { log.warn("softDeleteProduct - productId={} not found", productId); return false; });
    }
}
