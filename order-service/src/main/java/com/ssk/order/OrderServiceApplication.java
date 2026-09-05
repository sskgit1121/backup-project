package com.ssk.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("    SSK ORDER MANAGEMENT (PORT 8084) STARTING...     ");
        System.out.println("=====================================================");
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
