package com.ssk.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer // Activates the discovery server engine
public class ServiceRegistryApplication {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   SSK SERVICE REGISTRY IS STARTING...   ");
        System.out.println("=========================================");
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
