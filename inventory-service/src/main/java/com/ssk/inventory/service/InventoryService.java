package com.ssk.inventory.service;

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

    @Transactional              //Keep transactional at service layer
    public Inventory deductStock(String sku, Integer quantity) {
        // The aspect automatically forces Hibernate to attach: WHERE tenant_id = :tenantId
        Inventory inventory = inventoryRepository.findBySkuCode(sku)
                .orElseThrow(() -> new RuntimeException("SKU " + sku + " not found or access denied"));

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock level for SKU: " + sku);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory adjustStock(Inventory adjustmentPayload) {
        // Clean implicit filtering ensures safe lookup without mixing parameters
        Inventory existing = inventoryRepository.findBySkuCode(adjustmentPayload.getSkuCode())
                .orElseThrow(() -> new RuntimeException("SKU not found or access denied"));
        
        existing.setQuantity(adjustmentPayload.getQuantity());
        return inventoryRepository.save(existing);
    }
}
