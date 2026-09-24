package com.ssk.shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShippingServiceApplication {
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("    SSK SHIPPING & LOGISTICS (PORT 8087) STARTING!... ");
        System.out.println("=====================================================");
        SpringApplication.run(ShippingServiceApplication.class, args);
    }
}
