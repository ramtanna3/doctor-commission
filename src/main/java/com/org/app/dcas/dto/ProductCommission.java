package com.org.app.dcas.dto;

import java.math.BigDecimal;

public class ProductCommission {
    private Long productId;
    private BigDecimal commissionPercentage;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public BigDecimal getCommissionPercentage() { return commissionPercentage; }
    public void setCommissionPercentage(BigDecimal commissionPercentage) { this.commissionPercentage = commissionPercentage; }
}
