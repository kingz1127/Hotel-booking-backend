package com.example.hotel_booking_system_backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        // Ensure the secret is at least 256 bits (32 bytes) for HS256
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long extractAdminId(String token) {
        return extractClaim(token, claims -> claims.get("adminId", Long.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // Changed from parserBuilder() in 0.12.0+
                .build()
                .parseSignedClaims(token)      // Changed from parseClaimsJws() in 0.12.0+
                .getPayload();                 // Changed from getBody() in 0.12.0+
    }
}