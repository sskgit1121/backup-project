package com.ssk.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ssk.auth", "com.ssk.config"}) 
public class AuthServiceApplication {
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("    SSK IDENTITY & AUTH ENGINE (PORT 8081) STARTING... ");
        System.out.println("=====================================================");
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
