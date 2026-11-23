
package com.ssk.config;
//This is an AspectJ class that acts as a runtime proxy over your data layer
//enables the filter on a Hibernate Session before service methods execute: where tenant_id?
import com.ssk.context.TenantContext;

import jakarta.persistence.EntityManager;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import org.hibernate.Session;

import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantSecurityAspect {

    private final EntityManager entityManager;

    public TenantSecurityAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Before("execution(* com.ssk..service..*.*(..))")
    public void bindTenantFilterRuntimeContext() {

        String tenantId = TenantContext.getTenantId();

        if (tenantId == null || tenantId.isBlank()) {
            return;
        }

        Session session = entityManager.unwrap(Session.class);

        session.enableFilter("tenantFilter")
                .setParameter("tenantId", tenantId);
    }
}

