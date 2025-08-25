package com.pi.siabank.apigateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String TOKEN_TYPE_ACCESS = "access";

    public void validateToken(final String token, final String secret) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(secret))
                    .build()
                    .parseClaimsJws(token);
            log.info("Token validation successful");
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT signature", e);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new RuntimeException("Token validation failed", e);
        }
    }

    public String extractUsername(final String token, final String secret) {
        return extractClaim(token, secret, Claims::getSubject);
    }

    public String extractTokenType(final String token, final String secret) {
        return extractClaim(token, secret, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    public boolean isAccessToken(final String token, final String secret) {
        String type = extractTokenType(token, secret);
        return TOKEN_TYPE_ACCESS.equals(type);
    }

    private <T> T extractClaim(String token, String secret, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, secret);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(final String token, final String secret) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
