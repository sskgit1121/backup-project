package com.ssk.inventory.repository;

import com.ssk.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    //Optional<Inventory> findBySkuCodeAndTenantId(String skuCode, String tenantId);

	// KEEP ONLY THIS: The AOP aspect and Hibernate filter handle the tenant isolation automatically!
    Optional<Inventory> findBySkuCode(String skuCode);
}
