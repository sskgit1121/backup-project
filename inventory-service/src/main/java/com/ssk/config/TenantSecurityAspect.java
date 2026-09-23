
package com.ssk.config;

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

    @Before("execution(* com.ssk..repository..*.*(..))")
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

