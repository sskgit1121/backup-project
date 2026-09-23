package com.ssk.config;

import java.util.Optional;

// CRUCIAL: Ensure this exact import path is used, NOT any other dependency
import org.springframework.data.domain.AuditorAware; 
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

public class SecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
    	 System.out.println("--- JPA Auditing Listener Triggered ---");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() 
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Optional.of("SYSTEM");
        }

        // Extracts the username (subject claim) from the authenticated JWT
        return Optional.ofNullable(jwt.getSubject());
    }

}
