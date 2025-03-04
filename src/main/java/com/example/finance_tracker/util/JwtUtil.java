package com.example.finance_tracker.util;

import com.example.finance_tracker.model.User;
import com.example.finance_tracker.service.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(UserDetails userDetails, String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userDetails.getUsername());
        claims.put("userId", userId);
        claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        final String userId = extractUserId(token);

        logger.info("Validating token for user: {}", username);
        logger.debug("Extracted user ID from token: {}", userId);
        logger.debug("UserDetails user ID: {}", ((User) userDetails).getId());

        // Validate username, user ID, and token expiration
        boolean isUsernameValid = username.equals(userDetails.getUsername());
        boolean isUserIdValid = userId.equals(((User) userDetails).getId());
        boolean isTokenNotExpired = !isTokenExpired(token);

        if (!isUsernameValid) {
            logger.error("Token validation failed: Username mismatch. Expected: {}, Actual: {}", userDetails.getUsername(), username);
        }
        if (!isUserIdValid) {
            logger.error("Token validation failed: User ID mismatch. Expected: {}, Actual: {}", ((User) userDetails).getId(), userId);
        }
        if (!isTokenNotExpired) {
            logger.error("Token validation failed: Token is expired.");
        }

        boolean isValid = isUsernameValid && isUserIdValid && isTokenNotExpired;

        if (isValid) {
            logger.info("Token validation successful for user: {}", username);
        } else {
            logger.warn("Token validation failed for user: {}", username);
        }

        return isValid;
    }
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
}