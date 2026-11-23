
package com.ssk.inventory.repository;

import com.ssk.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findBySkuCodeAndTenantId(
            String skuCode, String tenantId);

    //explicit tenant-aware repository methods should be your primary isolation mechanism not AOP
    boolean existsBySkuCodeAndTenantId(
            String skuCode, String tenantId);

    List<Inventory> findAllByTenantId(String tenantId);
}