package com.ssk.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentServiceApplication {
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("    SSK PAYMENT SERVICES (PORT 8085) STARTING...     ");
        System.out.println("=====================================================");
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
