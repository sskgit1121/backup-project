package com.ssk.security;

import com.ssk.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TenantIdentifierFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(TenantIdentifierFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String tenantId = jwt.getClaimAsString("tenant_id");
                if (tenantId != null) {
                    TenantContext.setTenantId(tenantId);
                } else {
                    log.warn("Incoming JWT detected, but matching 'tenant_id' claim string was missing.");
                }
            }
        } catch (Exception e) {
            log.error("Failed to safely extract tenant extraction identifiers from JWT token contexts", e);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always purge thread states to prevent tenant leak vectors
            TenantContext.clear();
        }
    }
}
