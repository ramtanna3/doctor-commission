package com.org.app.dcas.service;

import com.org.app.dcas.model.ProductMaster;
import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.repository.ProductMasterRepository;
import com.org.app.dcas.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductMasterRepository productRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public ProductService(ProductMasterRepository productRepository, CompanyRepository companyRepository, CompanyContext companyContext) {
        this.productRepository = productRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    public List<ProductMaster> getAllProductsForCurrentCompany() {
        return productRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
    }

    public List<ProductMaster> getActiveProductsForCurrentCompany() {
        return productRepository.findByCompanyIdAndIsActiveTrue(companyContext.getCompanyId());
    }

    public ProductMaster getProductByIdForCurrentCompany(Long id) {
        return productRepository.findByProductIdAndCompanyId(id, companyContext.getCompanyId()).orElse(null);
    }

    public ProductMaster createProductForCurrentCompany(ProductMaster product) {
        Long companyId = companyContext.getCompanyId();
        product.setCompany(companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid company id")));
        return productRepository.save(product);
    }

    public ProductMaster updateProductForCurrentCompany(Long id, ProductMaster product) {
        return productRepository.findByProductIdAndCompanyId(id, companyContext.getCompanyId()).map(existing -> {
            existing.setName(product.getName());
            existing.setDescription(product.getDescription());
            // ...other fields as needed...
            return productRepository.save(existing);
        }).orElse(null);
    }

    public boolean softDeleteProductForCurrentCompany(Long id) {
        return productRepository.findByProductIdAndCompanyId(id, companyContext.getCompanyId()).map(existing -> {
            existing.setIsActive(false);
            productRepository.save(existing);
            return true;
        }).orElse(false);
    }
}
