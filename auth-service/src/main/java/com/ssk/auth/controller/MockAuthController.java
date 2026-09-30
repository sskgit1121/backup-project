package com.ssk.auth.controller;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.ssk.auth.repository.UserAuthorizationRepository;
import com.ssk.security.LocalKeyManager;

@RestController
@RequestMapping("/api/v1/auth")
public class MockAuthController {
	
	@Autowired
    private LocalKeyManager keyManager;
	 
	@Autowired
	private UserAuthorizationRepository authorizationRepository; // ➕ INJECT REPOSITORY
	
	//private  RoleRepository roleRepository;
	// Injecting the exact property used by your SecurityConfig
    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String secretKey;

    @PostMapping("/login/mock")
    public Map<String, String> generateMockJwtToken(
            @RequestParam String username,
            @RequestParam String tenantId) {

    	// ➕ Fetch permissions dynamically from your tables
        List<String> permissionList = authorizationRepository.findPermissionsByUsernameAndTenant(username, tenantId);
        List<String> roleList = authorizationRepository.findRolesByUsernameAndTenant(username, tenantId); 

        String token = createMockSignedJwt(username, tenantId, permissionList, roleList);

        Map<String, String> response = new HashMap<>();
        response.put("username", username);
        response.put("tenant_id", tenantId);
        response.put("permissions", String.join(",", permissionList));
        response.put("roleList", String.join(",", roleList));
        response.put("token_type", "Bearer");
        response.put("access_token", token);

        return response;
    }

 // 🔄 Change the method signature to accept the permissions list
    private String createMockSignedJwt(String username, String tenantId, List<String> permissionList, List<String> roleList) {
    	try {
            // 1. Build claims structure exactly how Spring Security expects it
            JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                    .issuer("mock-auth-server")
                    .subject(username)
                    .claim("tenant_id", tenantId)
                    .claim("roles", roleList)               // Sets "roles" from database query
                    .claim("permissions", permissionList) //  from db
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();

            // 2. Reuse standard Nimbus framework configurations to safely sign it via HS256
            JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
            
            // 3. Construct a standard encoder leveraging your configured secret key
            NimbusJwtEncoder encoder = new NimbusJwtEncoder(
                new com.nimbusds.jose.jwk.source.ImmutableSecret<>(
                    secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                )
            );

            // 4. Return perfectly formatted token string
            return encoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to create mock JWT token", e);
        }
    }
    
    /*
     * 
     * 
     *
     */
    //Asymetric JWT Token Jeneration and Expose Public key
    
    // 1. Endpoint to publish the Public Keys (Acts like Keycloak's certs endpoint)
    @GetMapping("/certs")
    public Map<String, Object> getPublicCerts() {
        return keyManager.getJwkSet().toJSONObject();
    }

    // 2. Endpoint to generate the Asymmetric RS256 Token
    @PostMapping("/login/asymmetric")
    public Map<String, String> generateAsymmetricToken(
            @RequestParam String username,
            @RequestParam String tenantId) {

    	/// ➕ Fetch permissions dynamically from your tables
        List<String> permissionList = authorizationRepository.findPermissionsByUsernameAndTenant(username, tenantId);
        List<String> roleList = authorizationRepository.findRolesByUsernameAndTenant(username, tenantId); 
        
        // Build claims exactly like your symmetric layout
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("http://localhost:8081") // Matches auth-service port
                .subject(username)
                .claim("tenant_id", tenantId)
                .claim("roles", roleList)          // Will contain ["ROLE_TENANT_ADMIN"] from db
                .claim("permissions", permissionList) //  FIXED: Will now contain ["inventory:read", "inventory:write"] from db
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Assign the unique key ID and use the RS256 algorithm
        JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(keyManager.getRsaKey().getKeyID())
                .build();

        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(keyManager.getJwkSet()));
        String token = encoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();

        Map<String, String> response = new HashMap<>();
        response.put("token_type", "Bearer");
        response.put("access_token", token);
        return response;
    }
}
