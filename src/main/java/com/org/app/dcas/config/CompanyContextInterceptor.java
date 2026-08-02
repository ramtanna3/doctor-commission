package com.org.app.dcas.config;

import com.org.app.dcas.context.CompanyContext;
import com.org.app.dcas.service.CompanyScopedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CompanyContextInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CompanyContextInterceptor.class);

    private final CompanyScopedService companyScopedService;
    private final CompanyContext companyContext;
    private final JwtUtil jwtUtil;

    public CompanyContextInterceptor(CompanyScopedService companyScopedService,
                                     CompanyContext companyContext,
                                     JwtUtil jwtUtil) {
        this.companyScopedService = companyScopedService;
        this.companyContext = companyContext;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("preHandle - missing or malformed Authorization header for {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isValid(token)) {
            log.warn("preHandle - invalid/expired JWT for {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return false;
        }

        try {
            Long userId = jwtUtil.getUserId(token);
            Long companyId = companyScopedService.getCompanyIdForUser(userId);
            if (companyId == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Company not found for user");
                return false;
            }
            companyContext.setCompanyId(companyId);
            companyContext.setUserId(userId);
            log.debug("preHandle - JWT valid: userId={}, companyId={}", userId, companyId);
        } catch (Exception e) {
            log.warn("preHandle - error resolving company: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid user or company");
            return false;
        }
        return true;
    }
}
