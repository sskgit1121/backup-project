package com.ssk.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("    SSK API GATEWAY ENGINE (PORT 8080) STARTING...   ");
        System.out.println("=====================================================");
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

