package com.ssk.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "INVENTORY-SERVICE")
public interface InventoryClient {

    @GetMapping("/api/inventory/check")
    boolean isInStock(@RequestParam("skuCode") String skuCode, @RequestParam("quantity") int quantity);
}
