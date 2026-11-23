package com.ssk.inventory.service;

import com.ssk.context.TenantContext;
import com.ssk.inventory.model.Inventory;
import com.ssk.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }


@Transactional
public Inventory deductStock(String sku, Integer quantity) {
    String tenantId = TenantContext.requireTenantId();

    Inventory inventory = inventoryRepository
            .findBySkuCodeAndTenantId(sku, tenantId)
            .orElseThrow(() ->
                    new RuntimeException(
                            "SKU not found or access denied"));

    if (inventory.getQuantity() < quantity) {
        throw new RuntimeException(
                "Insufficient stock level for SKU: " + sku);
    }

    inventory.setQuantity(
            inventory.getQuantity() - quantity);

    return inventoryRepository.save(inventory);
}

@Transactional
public Inventory adjustStock(Inventory adjustmentPayload) {

    // Get the tenant ID established from the authenticated request.
    String tenantId = TenantContext.getTenantId();

    // Reject requests without a valid tenant context.
    if (tenantId == null || tenantId.isBlank()) {
        throw new RuntimeException("Tenant context is missing");
    }

    // Find the SKU only within the current tenant.
    Inventory existing = inventoryRepository
            .findBySkuCodeAndTenantId(
                    adjustmentPayload.getSkuCode(),
                    tenantId
            )
            .orElseThrow(() ->
                    new RuntimeException("SKU not found or access denied"));

    // Update the existing record's quantity.
    existing.setQuantity(adjustmentPayload.getQuantity());

    // Persist and return the updated inventory.
    return inventoryRepository.save(existing);
}
}
