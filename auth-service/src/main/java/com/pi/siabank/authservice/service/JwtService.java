package com.pi.siabank.authservice.service;

import com.pi.siabank.authservice.model.RefreshToken;
import com.pi.siabank.authservice.model.User;
import com.pi.siabank.authservice.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String CLAIM_AUDIENCE = "aud";
    private static final String CLAIM_JTI = "jti";

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration.access-token}")
    private long ACCESS_TOKEN_EXPIRATION; // e.g., 600000 (10 minutes)

    @Value("${jwt.expiration.refresh-token}")
    private long REFRESH_TOKEN_EXPIRATION; // e.g., 604800000 (7 days)

    @Value("${spring.application.name}")
    private String issuer;

    private final RefreshTokenRepository refreshTokenRepository;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(extractTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(extractTokenType(token));
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = accessTokenClaims(userDetails);
        return buildToken(extraClaims, userDetails, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateAndSaveRefreshToken(User userDetails) {
        UUID jti = UUID.randomUUID();
        Map<String, Object> extraClaims = refreshTokenClaims(jti.toString());
        String refreshToken = buildToken(extraClaims, userDetails, REFRESH_TOKEN_EXPIRATION);
        Instant expiryDate = Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION);

        refreshTokenRepository.save(RefreshToken.builder()
                .refreshToken(refreshToken)
                .user((User) userDetails)
                .expiryDate(expiryDate)
                .build());

        return refreshToken;
    }

    private Map<String, Object> accessTokenClaims(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        claims.put(CLAIM_AUDIENCE, "api");
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        if (authorities != null) {
            List<String> roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            claims.put("roles", roles);
        }
        return claims;
    }

    private Map<String, Object> refreshTokenClaims(String jti) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
        claims.put(CLAIM_AUDIENCE, "auth");
        claims.put(CLAIM_JTI, jti);
        return claims;
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails) && isRefreshToken(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
