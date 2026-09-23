package com.ssk.config;

import com.ssk.context.TenantContext;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantSecurityAspect {

    @Autowired
    private EntityManager entityManager;

    // Catches execution sequences targeting all local sub-repositories 
    @Before("execution(* com.ssk..repository..*.*(..))")
    public void bindTenantFilterRuntimeContext() {
        String activeTenantId = TenantContext.getTenantId();
        
        if (activeTenantId != null) {
            Session nativeSession = entityManager.unwrap(Session.class);
            // Applies parameter assertions cleanly behind the scenes
            nativeSession.enableFilter("tenantFilter")
                         .setParameter("tenantId", activeTenantId);
        }
    }
}
