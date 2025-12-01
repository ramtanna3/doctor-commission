package com.org.app.dcas.context;

import org.springframework.stereotype.Component;

@Component
public class CompanyContext {
    private Long companyId;
    private Long userId;

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
