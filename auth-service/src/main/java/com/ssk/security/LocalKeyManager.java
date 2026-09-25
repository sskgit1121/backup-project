package com.ssk.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Component
public class LocalKeyManager {

    private final RSAKey rsaKey;

    public LocalKeyManager() {
        try {
            // Generate a secure 2048-bit RSA key pair in memory on startup without keycloak 
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            this.rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize local RSA keys", e);
        }
    }

    public RSAKey getRsaKey() {
        return this.rsaKey;
    }

    public JWKSet getJwkSet() {
        return new JWKSet(this.rsaKey);
    }
}
