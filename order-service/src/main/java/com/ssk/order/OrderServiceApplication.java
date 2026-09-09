package com.ssk.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients // <-- Crucial: This tells Spring to look for Feign interfaces
public class OrderServiceApplication {
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("    SSK ORDER MANAGEMENT (PORT 8084) STARTING...     ");
        System.out.println("=====================================================");
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
