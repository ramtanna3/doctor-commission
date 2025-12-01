package com.org.app.dcas.controller;

import com.org.app.dcas.model.ProductMaster;
import com.org.app.dcas.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductMaster> getAllProducts() {
        return productService.getAllProductsForCurrentCompany();
    }

    @GetMapping("/active")
    public List<ProductMaster> getAllActiveProducts() {
        return productService.getActiveProductsForCurrentCompany();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductMaster> getProductById(@PathVariable Long id) {
        ProductMaster product = productService.getProductByIdForCurrentCompany(id);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ProductMaster> createProduct(@RequestBody ProductMaster product) {
        ProductMaster saved = productService.createProductForCurrentCompany(product);
        return ResponseEntity.created(URI.create("/api/products/" + saved.getProductId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductMaster> updateProduct(@PathVariable Long id, @RequestBody ProductMaster product) {
        ProductMaster updated = productService.updateProductForCurrentCompany(id, product);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable Long id) {
        boolean deleted = productService.softDeleteProductForCurrentCompany(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}