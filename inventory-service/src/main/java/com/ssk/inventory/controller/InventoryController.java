
package com.ssk.inventory.controller;

import com.ssk.context.TenantContext;
import com.ssk.inventory.model.Inventory;
import com.ssk.inventory.repository.InventoryRepository;
import com.ssk.inventory.service.InventoryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/erp/inventory")
public class InventoryController {

	 private final InventoryRepository inventoryRepository;
	    private final InventoryService inventoryService;

	    public InventoryController(InventoryRepository inventoryRepository, InventoryService inventoryService) {
	        this.inventoryRepository = inventoryRepository;
	        this.inventoryService = inventoryService;
	    }

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAuthority('inventory:write')")
	public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {

		String tenantId = TenantContext.requireTenantId();

		if (inventoryRepository.existsBySkuCodeAndTenantId(
		        inventory.getSkuCode(), tenantId)) {
		    throw new IllegalArgumentException(
		            "Inventory SKU already exists for this tenant");
		}

		Inventory savedInventory = inventoryRepository.save(inventory);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedInventory);
	}

	@PostMapping("/deduct/{sku}/{quantity}")
	@PreAuthorize("hasAuthority('inventory:write')")
	public ResponseEntity<?> deductInventory(
	        @PathVariable("sku") String sku,
	        @PathVariable("quantity") Integer quantity) {

	    if (quantity == null || quantity <= 0) {
	        throw new IllegalArgumentException(
	                "Deduction quantity must be greater than zero");
	    }

	    Inventory updated = inventoryService.deductStock(
	            sku, quantity);

	    Map<String, Object> response = new HashMap<>();
	    response.put("sku", sku);
	    response.put("deductedQty", quantity);
	    response.put("remainingStock", updated.getQuantity());
	    response.put("status", "SUCCESS");

	    return ResponseEntity.ok(response);
	}

	@PostMapping("/test-header")
	public ResponseEntity<String> testHeader(
	        @RequestHeader(
	                value = "Authorization",
	                required = false) String authHeader) {
	    return ResponseEntity.ok(
	            "Received Authorization Header: " + authHeader);
	}


@GetMapping("/check")
@PreAuthorize("hasAuthority('inventory:read')")
public boolean isInStock(
        @RequestParam String skuCode,
        @RequestParam int quantity) {

    if (quantity < 0) {
        throw new IllegalArgumentException(
                "Quantity cannot be negative");
    }

    String tenantId = TenantContext.requireTenantId();

    return inventoryRepository
            .findBySkuCodeAndTenantId(skuCode, tenantId)
            .map(item -> item.getQuantity() >= quantity)
            .orElse(false);
}

@GetMapping
@PreAuthorize("hasAuthority('inventory:read')")
public List<Inventory> fetchAllStock() {
    return inventoryRepository.findAllByTenantId(
            TenantContext.requireTenantId());
}

	    @PostMapping("/adjust")
	    @PreAuthorize("hasAuthority('inventory:write')")
	    public ResponseEntity<Inventory> modifyStock(@RequestBody Inventory adjustmentPayload) {
	        Inventory updatedInventory = inventoryService.adjustStock(adjustmentPayload);
	        return ResponseEntity.ok(updatedInventory);
	    }
	}
