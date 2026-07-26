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
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductMasterRepository productRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public ProductService(ProductMasterRepository productRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<ProductMaster> getAllProductsForCurrentCompany() {
        Long companyId = companyContext.getCompanyId();
        log.info("getAllProducts - companyId={}", companyId);
        List<ProductMaster> result = productRepository.findByCompanyIdAndIsActiveTrue(companyId);
        log.info("getAllProducts - returned {} products", result.size());
        return result;
    }

    public List<ProductMaster> getActiveProductsForCurrentCompany() {
        return getAllProductsForCurrentCompany();
    }

    public ProductMaster getProductByIdForCurrentCompany(Long id) {
        log.info("getProductById - productId={}", id);
        ProductMaster result = productRepository.findByProductIdAndCompanyId(id, companyContext.getCompanyId()).orElse(null);
        if (result == null) log.warn("getProductById - productId={} not found", id);
        return result;
    }

    public ProductMaster createProductForCurrentCompany(ProductMaster product) {
        Long companyId = companyContext.getCompanyId();
        log.info("createProduct - companyId={}, name='{}'", companyId, product.getName());
        product.setCompany(companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        ProductMaster saved = productRepository.save(product);
        log.info("createProduct - created productId={}", saved.getProductId());
        return saved;
    }

    public ProductMaster updateProductForCurrentCompany(Long id, ProductMaster product) {
        log.info("updateProduct - productId={}", id);
        return productRepository.findByProductIdAndCompanyId(id, companyContext.getCompanyId()).map(existing -> {
            existing.setName(product.getName());
            existing.setDescription(product.getDescription());
            ProductMaster saved = productRepository.save(existing);
            log.info("updateProduct - productId={} updated", id);
            return saved;
        }).orElseGet(() -> { log.warn("updateProduct - productId={} not found", id); return null; });
    }

    public boolean softDeleteProductForCurrentCompany(Long id) {
        log.info("softDeleteProduct - productId={}", id);
        return productRepository.findByProductIdAndCompanyId(id, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            productRepository.save(existing);
            log.info("softDeleteProduct - productId={} deactivated", id);
            return true;
        }).orElseGet(() -> { log.warn("softDeleteProduct - productId={} not found", id); return false; });
    }
}
