package com.ssk.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {
    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        log.debug("Setting Tenant Context ID to: {}", tenantId);
        currentTenant.set(tenantId);
    }

    public static String getTenantId() {
        return currentTenant.get();
    }

    public static void clear() {
        log.debug("Clearing active Tenant Context ThreadLocal allocation.");
        currentTenant.remove(); // Essential: Prevents thread-pool memory leaks
    }
}
