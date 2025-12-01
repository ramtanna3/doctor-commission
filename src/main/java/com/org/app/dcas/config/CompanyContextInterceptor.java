package com.org.app.dcas.config;

import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.service.CompanyScopedService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CompanyContextInterceptor implements HandlerInterceptor {

    private final CompanyScopedService companyScopedService;
    private final CompanyContext companyContext;

    public CompanyContextInterceptor(CompanyScopedService companyScopedService, CompanyContext companyContext) {
        this.companyScopedService = companyScopedService;
        this.companyContext = companyContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String userIdHeader = request.getHeader("x-user-id");
        if (userIdHeader == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing x-user-id header");
            return false;
        }
        try {
            Long userId = Long.valueOf(userIdHeader);
            Long companyId = companyScopedService.getCompanyIdForUser(userId);
            if (companyId == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Company not found for user");
                return false;
            }
            companyContext.setCompanyId(companyId);
            companyContext.setUserId(userId); // Set userId in context
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid user or company");
            return false;
        }
        return true;
    }
}
