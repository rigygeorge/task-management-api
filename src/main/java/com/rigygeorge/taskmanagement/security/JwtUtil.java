package com.rigygeorge.taskmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    // Generate token for user
    public String generateToken(UUID userId, String email, String role, UUID tenantId) {
        Map<String, Object> claims = new HashMap<>();
        // Custom claims
        claims.put("userId", userId.toString());
        claims.put("email", email);
        claims.put("role", role);
        claims.put("tenantId", tenantId.toString());
        
        return createToken(claims, email);
    }
    
    // Create JWT token
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(claims) // FIX 1: New API method instead of deprecated .setClaims()
                .subject(subject) // FIX 2: Modern replacement for .setSubject()
                .issuedAt(now)    // Modern replacement for .setIssuedAt()
                .expiration(expiryDate) // Modern replacement for .setExpiration()
                .signWith(getSigningKey(), Jwts.SIG.HS256) // FIX 3: Use Jwts.SIG for algorithm constants
                .compact();
    }
    
    // Get signing key
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    // Extract username (email) from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // Extract user ID from token
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return UUID.fromString(userIdStr);
    }
    
    // Extract tenant ID from token
    public UUID extractTenantId(String token) {
        String tenantIdStr = extractClaim(token, claims -> claims.get("tenantId", String.class));
        return UUID.fromString(tenantIdStr);
    }
    
    // Extract role from token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    // Extract expiration date
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    // Extract specific claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    // Extract all claims (The core of token parsing and verification)
    private Claims extractAllClaims(String token) {
        // FIX 4: Use Jwts.parser() and the modern method .verifyWith(key)
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getSigningKey()) // FIX: Use verifyWith(key) instead of deprecated setSigningKey()
                .build()
                .parseSignedClaims(token); // FIX: Use .parseSignedClaims() for JWS tokens
        
        return jws.getPayload(); // Get the claims body from the Jws object
    }
    
    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    // Validate token
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}