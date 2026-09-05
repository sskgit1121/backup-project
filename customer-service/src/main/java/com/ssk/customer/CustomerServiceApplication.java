package com.ssk.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient // 🚀 Forces client registration tracking
public class CustomerServiceApplication {

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("    SSK CUSTOMER SERVICE (PORT 8082) STARTING...     ");
        System.out.println("=====================================================");
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
