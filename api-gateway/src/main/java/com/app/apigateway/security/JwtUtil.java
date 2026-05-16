package com.app.apigateway.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    private Key key;

   private Key key() {
    if (key == null) {
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    return key;
}

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token) {
        Integer userId = extractClaims(token).get("userId", Integer.class);
        return userId == null ? null : userId.longValue();
    }

    public boolean validateToken(String token) {
        try {
            return extractClaims(token)
                    .getExpiration()
                    .after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
