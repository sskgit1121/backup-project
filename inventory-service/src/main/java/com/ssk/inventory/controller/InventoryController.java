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

    @GetMapping("/check")
    public boolean isInStock(@RequestParam String skuCode, @RequestParam int quantity) {
        return inventoryRepository.findBySkuCode(skuCode)
                .map(inventory -> inventory.getQuantity() >= quantity) // 🔑 Changed from .quantity()
                .orElse(false);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inventory createInventory(@RequestBody Inventory inventory) {
        // Simple duplication check before inserting
        if (inventoryRepository.findBySkuCode(inventory.getSkuCode()).isPresent()) {
            throw new RuntimeException("Inventory with SKU " + inventory.getSkuCode() + " already exists!");
        }
        return inventoryRepository.save(inventory);
    }
}
