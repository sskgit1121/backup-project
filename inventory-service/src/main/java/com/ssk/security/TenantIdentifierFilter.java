package com.ssk.security;

import com.ssk.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TenantIdentifierFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
                                    throws ServletException, IOException {
        try {
            // 1. Get the current authentication object populated by BearerTokenAuthenticationFilter
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                // 2. Extract tenant_id claim from the validated mock JWT token
                String tenantId = jwt.getClaimAsString("tenant_id");
                
                if (tenantId != null && !tenantId.isBlank()) {
                    TenantContext.setTenantId(tenantId);
                }
            }

            // 3. Continue processing the request
            filterChain.doFilter(request, response);
            
        } finally {
            // 4. CRUCIAL: Always clear the thread local to prevent memory leaks in shared thread pools
            TenantContext.clear();
        }
    }
}
