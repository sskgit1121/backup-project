package com.ssk.security;

import com.ssk.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantIdentifierFilter extends OncePerRequestFilter {


@Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

    try {
        // Authentication should already be available after
        // BearerTokenAuthenticationFilter.
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.getPrincipal() instanceof Jwt jwt) {

            String tenantId = jwt.getClaimAsString("tenant_id");

            if (tenantId == null || tenantId.isBlank()) {
                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Missing or invalid tenant claim"
                );
                return;
            }

            TenantContext.setTenantId(tenantId);
        }

        // Continue the request exactly once.
        filterChain.doFilter(request, response);

    } finally {
        // Prevent tenant context from leaking between requests.
        TenantContext.clear();
    }
}
}
