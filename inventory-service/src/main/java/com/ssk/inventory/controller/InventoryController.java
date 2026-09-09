package com.ssk.inventory.controller;

import com.ssk.inventory.model.Inventory;
import com.ssk.inventory.repository.InventoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    public InventoryController(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    // 1. Existing GET endpoint for checking stock
    @GetMapping("/check")
    public boolean isInStock(@RequestParam String skuCode, @RequestParam int quantity) {
        return inventoryRepository.findBySkuCode(skuCode)
                .map(inventory -> inventory.quantity() >= quantity)
                .orElse(false);
    }

    // 2. ADD THIS NEW POST ENDPOINT TO SEED DATA
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryRepository.save(inventory);
    }
}
