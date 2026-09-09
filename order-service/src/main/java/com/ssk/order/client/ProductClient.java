package com.ssk.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Value must match the exact application name registered in Eureka
@FeignClient(name = "PRODUCT-SERVICE") 
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductResponse getProductById(@PathVariable("id") String id);
}

// Simple record or DTO to map the returning JSON data
//record ProductResponse(String id, String name, double price) {}
