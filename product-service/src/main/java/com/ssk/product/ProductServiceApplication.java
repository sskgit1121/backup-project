package com.ssk.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProductServiceApplication {
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("    SSK PRODUCT SERVICE (PORT 8083) STARTING...      ");
        System.out.println("=====================================================");
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
