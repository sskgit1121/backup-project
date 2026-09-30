package com.ssk.config;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import com.nimbusds.jose.util.JSONArrayUtils;
import com.nimbusds.jwt.JWTParser;
import java.util.Map;
import java.util.function.Function;

public class DelegatingJwtDecoder implements JwtDecoder {

    private final Function<String, JwtDecoder> decoderResolver;

    public DelegatingJwtDecoder(Function<String, JwtDecoder> decoderResolver) {
        this.decoderResolver = decoderResolver;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            // Inspect the JWT header algorithm type at runtime without parsing the signature first
            String algorithm = (String) JWTParser.parse(token).getHeader().toJSONObject().get("alg");
            
            if (algorithm == null) {
                throw new JwtException("Missing signature algorithm 'alg' in token header mapping.");
            }

            // Route execution down to the mapped engine block (RS256 vs HS256)
            JwtDecoder delegate = decoderResolver.apply(algorithm);
            if (delegate == null) {
                throw new JwtException("No matching decoder configured for token signature algorithm: " + algorithm);
            }

            return delegate.decode(token);
        } catch (Exception e) {
            throw new JwtException("Failed to decode dynamic multi-tenant hybrid token: " + e.getMessage(), e);
        }
    }
}
