package com.ssk.inventory.repository;


import com.ssk.inventory.model.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface InventoryRepository extends MongoRepository<Inventory, String> {
    Optional<Inventory> findBySkuCode(String skuCode);
}
