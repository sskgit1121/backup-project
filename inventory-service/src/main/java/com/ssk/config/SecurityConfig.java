package com.ssk.config;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.ssk.security.TenantIdentifierFilter;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // ADDED: Activates your @PreAuthorize method annotations
public class SecurityConfig {

	//Symetric key
    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String secretKey;
    
  //Asymetric verify
 // Custom Local Asymmetric JWK Set URI pointing to auth-service/certs
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri; 
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF for stateless REST microservices
            .csrf(csrf -> csrf.disable()) 
            
            // 2. Clear out pre-auth 403 crashes and return clean errors (like 401 Unauthorized)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
            )
            
            // 3. Define access paths
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login/**", "/api/v1/erp/inventory/test-header" ,"/error").permitAll() // Keep auth endpoints open
                .anyRequest().authenticated()                                                                      // Secure everything else (like inventory)
            )
            
         //  CRUCIAL FIX: Bind the resolver explicitly to your Resource Server settings
            .oauth2ResourceServer(oauth2 -> oauth2
            	    .authenticationManagerResolver(customAuthenticationManagerResolver())
            	    .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
            	)

            
            // 5. CRUCIAL: Hook up your tenant filter right after Spring validates the token signature
            .addFilterAfter(
                new TenantIdentifierFilter(),
                BearerTokenAuthenticationFilter.class
            );

        return http.build();
    }

    /**
     * Resolves the proper validation manager per request dynamically based on token content or attributes.
     */
    private AuthenticationManagerResolver<HttpServletRequest> customAuthenticationManagerResolver() {
        JwtAuthenticationProvider symmetricProvider = new JwtAuthenticationProvider(jwtDecoder()); 
        JwtAuthenticationProvider asymmetricProvider = new JwtAuthenticationProvider(asymmetricJWTDecoder());

        //  UPDATED: Use the corrected method mapping call to assign the converter
        Converter<Jwt, ? extends AbstractAuthenticationToken> converter = downstreamJwtAuthenticationConverter();
        symmetricProvider.setJwtAuthenticationConverter(converter);
        asymmetricProvider.setJwtAuthenticationConverter(converter);

        return request -> {
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return symmetricProvider::authenticate;
            }
            String token = authorizationHeader.substring(7);
         // Wrap the raw string token explicitly into the expected container type
            org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken authToken = 
                new org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken(token);

            if (isAsymmetricToken(token)) {
                return authentication -> asymmetricProvider.authenticate(authToken);
            }
            return authentication -> symmetricProvider.authenticate(authToken);
        };
    }


 //  your existing converter method block with this one:
    public Converter<Jwt, ? extends AbstractAuthenticationToken> downstreamJwtAuthenticationConverter() {
        return jwt -> {
            // Temp debug to inspect incoming token claims
            System.out.println("=== DEBUG: YOUR TOKEN CLAIMS ===");
            jwt.getClaims().forEach((key, value) -> System.out.println(key + " -> " + value));
            System.out.println("================================");

            // FIX: Always use a mutable ArrayList here!
            List<GrantedAuthority> authorities = new java.util.ArrayList<>();

            // 1. Safely extract and copy custom roles
            Object rolesClaim = jwt.getClaim("roles");
            if (rolesClaim instanceof Collection<?>) {
                ((Collection<?>) rolesClaim).stream()
                        .map(Object::toString)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add); // Works perfectly on an ArrayList
            }

            // 2. Safely extract and copy custom permissions
            Object permissionsClaim = jwt.getClaim("permissions");
            if (permissionsClaim instanceof Collection<?>) {
                ((Collection<?>) permissionsClaim).stream()
                        .map(Object::toString)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            } else if (permissionsClaim instanceof String stringPermissions && !stringPermissions.isBlank()) {
                java.util.Arrays.stream(stringPermissions.split("[, ]+"))
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }

            // 3. Fallback to prevent access-denied errors if both lists are completely empty
            if (authorities.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            return new JwtAuthenticationToken(jwt, authorities);
        };
    }
    /**
     * Standard Symmetric JWT Decryption Logic
     */
    
    private JwtDecoder jwtDecoder() {
    	byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            secretKey.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );

        return NimbusJwtDecoder
            .withSecretKey(secretKeySpec)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }
   
    /**
     * Isolated Asymmetric OIDC Keycloak Realm Decoder
     */
    private JwtDecoder asymmetricJWTDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
        		.jwsAlgorithm(SignatureAlgorithm.RS256) 
                .build();        
    }


    /**
     * Fast structural hint check to separate Keycloak signatures from local mock items
     */
    private boolean isAsymmetricToken(String token) {
        try {
            // Parse the token using Nimbus's built-in engine
            JWT jwt = JWTParser.parse(token);
            // Grab the cryptographic algorithm directly from the header safely
            String algorithm = jwt.getHeader().toJSONObject().get("alg").toString();
            // Custom asymmetric architecture generates RS256. Symmetric uses HS256.
            return "RS256".equalsIgnoreCase(algorithm);
        } catch (Exception e) {
            // Fallback cleanly to your symmetric default if the token is completely malformed
            return false;
        }
    }
}
