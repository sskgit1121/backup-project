
package com.ssk.inventory.controller;

import com.ssk.context.TenantContext;
import com.ssk.inventory.model.Inventory;
import com.ssk.inventory.repository.InventoryRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/erp/inventory")
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    public InventoryController(
            InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping("/check")
    public boolean isInStock(
            @RequestParam String skuCode,
            @RequestParam int quantity) {

        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Quantity cannot be negative"
            );
        }

        return inventoryRepository.findBySkuCode(skuCode)
                .map(inventory ->
                        inventory.getQuantity() >= quantity
                )
                .orElse(false);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Inventory> createInventory(
            @RequestBody Inventory inventory) {

        if (inventoryRepository
                .findBySkuCode(inventory.getSkuCode())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Inventory with SKU "
                            + inventory.getSkuCode()
                            + " already exists"
            );
        }

        Inventory savedInventory = inventoryRepository.save(inventory); 
        return ResponseEntity.status(HttpStatus.CREATED).body(savedInventory);   
        }

    @PostMapping("/deduct/{sku}/{quantity}")
    @Transactional 
    public ResponseEntity<?> deductInventory(
        @PathVariable("sku") String sku, 
        @PathVariable("quantity") Integer quantity
    ) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Deduction quantity must be greater than zero"
            );
        }
        String activeTenant = TenantContext.getTenantId();

        if (activeTenant == null || activeTenant.isBlank()) {
            throw new SecurityException("Access Denied: Request execution context is missing a valid Tenant ID.");
        }
        Inventory inventory = inventoryRepository
                .findBySkuCodeAndTenantId(sku, activeTenant)
                .orElseThrow(() ->
                        new RuntimeException(
                                "SKU " + sku
                                        + " not found or access denied"
                        )
                );

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException(
                    "Insufficient stock level for SKU: " + sku
            );
        }

        inventory.setQuantity(
                inventory.getQuantity() - quantity
        );

        inventoryRepository.save(inventory);

        Map<String, Object> response = new HashMap<>();

        response.put("sku", sku);
        response.put("deductedQty", quantity);
        response.put("remainingStock", inventory.getQuantity());
        response.put("status", "SUCCESS");

        return ResponseEntity.ok(response); 
        }
    
    @PostMapping("/test-header")
    public ResponseEntity<String> testHeader(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok("Received Authorization Header: " + authHeader);
    }
}

