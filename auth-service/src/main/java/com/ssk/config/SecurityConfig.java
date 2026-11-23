package com.ssk.config;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import com.nimbusds.jose.JOSEException;
import com.ssk.security.LocalKeyManager;
import com.ssk.security.TenantIdentifierFilter;

@Configuration
public class SecurityConfig {

	//symetric key
    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String secretKey;
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;
    
    @Autowired
    private LocalKeyManager keyManager; 
    
    
    /**
     * Chain 1: Public endpoints.
     * Captures only specific paths and allows them through completely unauthenticated.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // Scope this filter chain strictly to public URL patterns
            .securityMatchers(matchers -> matchers
                .requestMatchers("/api/v1/auth/login/**","/api/v1/auth/certs", "/error")
            )
            // Disable CSRF for public endpoints
            .csrf(csrf -> csrf.disable()) 
            // Allow unauthenticated access to these routes
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
            
        return http.build();
    }

    /**
     * Chain 2: Protected endpoints Symmetric Legacy Fallback (Default Fallback).
     * Secures all other API routes using stateless JWT Bearer token authentication.
     * Secures all other back-channel/internal service requests via the shared secret key.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain protectedSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless REST APIs
            .csrf(csrf -> csrf.disable())

            // All requests passing through this chain must be authenticated
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )

            // Configure application as an OAuth2 Resource Server validating JWTs
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )

            // Extract the multi-tenant context right after token validation succeeds
            .addFilterAfter(
                new TenantIdentifierFilter(),
                BearerTokenAuthenticationFilter.class
            );

        return http.build();
    }
 //for permissions
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("permissions"); 
        grantedAuthoritiesConverter.setAuthorityPrefix(""); 

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
    /**
     * Local Symmetric Decoder instantiation logic:internal.
     * Removed the global @Bean marker so it doesn't conflict with Chain 2.
     * @throws JOSEException 
     */
    @Bean
    public JwtDecoder jwtDecoder() throws JOSEException {

        // RS256: verify tokens using the public key.
        NimbusJwtDecoder asymmetricDecoder = NimbusJwtDecoder
                .withPublicKey(keyManager.getRsaKey().toRSAPublicKey())
                .build();

        asymmetricDecoder.setJwtValidator(jwtValidators());

        // HS256: verify tokens using the shared secret.
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        NimbusJwtDecoder symmetricDecoder = NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        symmetricDecoder.setJwtValidator(jwtValidators());

        // Select the decoder according to the token's signing algorithm.
        Map<String, JwtDecoder> decoders = new HashMap<>();
        decoders.put("RS256", asymmetricDecoder);
        decoders.put("HS256", symmetricDecoder);

        return new DelegatingJwtDecoder(decoders::get);
    }
    //pass enco
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    private OAuth2TokenValidator<Jwt> jwtValidators() {

        return new DelegatingOAuth2TokenValidator<>(
                // Validate issuer, expiration and other standard JWT claims.
                JwtValidators.createDefaultWithIssuer(issuer),

                // Validate claims required by your multi-tenant application.
                jwt -> {
                    String tenantId = jwt.getClaimAsString("tenant_id");
                    Object roles = jwt.getClaim("roles");
                    Object permissions = jwt.getClaim("permissions");

                    boolean validTenant =
                            tenantId != null && !tenantId.isBlank();

                    boolean validRoles =
                            roles instanceof Collection<?>
                            && !((Collection<?>) roles).isEmpty();

                    boolean validPermissions =
                            (permissions instanceof Collection<?>
                                    && !((Collection<?>) permissions).isEmpty())
                            || (permissions instanceof String
                                    && !((String) permissions).isBlank());

                    if (validTenant && validRoles && validPermissions) {
                        return OAuth2TokenValidatorResult.success();
                    }

                    return OAuth2TokenValidatorResult.failure(
                            new OAuth2Error(
                                    "invalid_token",
                                    "Required tenant or authorization claims are missing",
                                    null
                            )
                    );
                }
        );
    }
}
