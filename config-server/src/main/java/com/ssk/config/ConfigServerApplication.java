package com.ssk.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConfigServerApplication {
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("    SSK CONFIG SERVER ENGINE (PORT 8888) STARTING... ");
        System.out.println("=====================================================");
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
