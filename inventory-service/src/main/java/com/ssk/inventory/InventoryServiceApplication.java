package com.ssk.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventoryServiceApplication {
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("    SSK INVENTORY MANAGEMENT (PORT 8086) STARTING... ");
        System.out.println("=====================================================");
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
