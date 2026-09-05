package com.ssk.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("    SSK NOTIFICATION HUB (PORT 8088) STARTING...     ");
        System.out.println("=====================================================");
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
