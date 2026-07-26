package com.org.app.dcas.controller;

import com.org.app.dcas.model.ProductMaster;
import com.org.app.dcas.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductMaster> getAllProducts() {
        log.info("GET /api/products - fetching all products for current company");
        List<ProductMaster> result = productService.getAllProductsForCurrentCompany();
        log.info("GET /api/products - returned {} products", result.size());
        return result;
    }

    @GetMapping("/active")
    public List<ProductMaster> getAllActiveProducts() {
        log.info("GET /api/products/active - fetching active products");
        List<ProductMaster> result = productService.getActiveProductsForCurrentCompany();
        log.info("GET /api/products/active - returned {} products", result.size());
        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductMaster> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{} - fetching product", id);
        ProductMaster product = productService.getProductByIdForCurrentCompany(id);
        if (product == null) { log.warn("GET /api/products/{} - not found", id); }
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ProductMaster> createProduct(@RequestBody ProductMaster product) {
        log.info("POST /api/products - creating product name='{}'", product.getName());
        ProductMaster saved = productService.createProductForCurrentCompany(product);
        log.info("POST /api/products - created productId={}", saved.getProductId());
        return ResponseEntity.created(URI.create("/api/products/" + saved.getProductId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductMaster> updateProduct(@PathVariable Long id, @RequestBody ProductMaster product) {
        log.info("PUT /api/products/{} - updating product", id);
        ProductMaster updated = productService.updateProductForCurrentCompany(id, product);
        if (updated == null) { log.warn("PUT /api/products/{} - not found", id); }
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{} - soft-deleting product", id);
        boolean deleted = productService.softDeleteProductForCurrentCompany(id);
        if (!deleted) { log.warn("DELETE /api/products/{} - not found", id); }
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}