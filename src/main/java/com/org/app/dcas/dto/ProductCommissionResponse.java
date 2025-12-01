package com.org.app.dcas.dto;

import com.org.app.dcas.model.ProductMaster;
import java.math.BigDecimal;

public class ProductCommissionResponse {
    private ProductMaster product;
    private BigDecimal commissionPercentage;

    public ProductCommissionResponse(ProductMaster product, BigDecimal commissionPercentage) {
        this.product = product;
        this.commissionPercentage = commissionPercentage;
    }

    public ProductMaster getProduct() { return product; }
    public void setProduct(ProductMaster product) { this.product = product; }

    public BigDecimal getCommissionPercentage() { return commissionPercentage; }
    public void setCommissionPercentage(BigDecimal commissionPercentage) { this.commissionPercentage = commissionPercentage; }
}
