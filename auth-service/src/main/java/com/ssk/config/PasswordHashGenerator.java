package com.ssk.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashGenerator implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    // Constructor injection — no Lombok required
    public PasswordHashGenerator(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        String hash = passwordEncoder.encode("HasedPassword");

        System.out.println("Generated BCrypt hash: " + hash);
    }
}