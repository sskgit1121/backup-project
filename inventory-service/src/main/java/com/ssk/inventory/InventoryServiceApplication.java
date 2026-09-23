package com.ssk.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {"com.ssk.inventory", "com.ssk.config"})
public class InventoryServiceApplication {
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("    SSK INVENTORY MANAGEMENT (PORT 8086) STARTING... ");
        System.out.println("=====================================================");
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
