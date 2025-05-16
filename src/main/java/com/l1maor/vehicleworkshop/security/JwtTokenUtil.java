package com.l1maor.vehicleworkshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);
    private final JwtProperties jwtProperties;
    
    public JwtTokenUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }
    
    private Key getSigningKey() {
        logger.debug("Generating JWT signing key");
        byte[] keyBytes = jwtProperties.getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(UserDetails userDetails) {
        logger.info("Generating JWT token for user: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());
        logger.debug("Creating JWT token for subject: {} with expiration: {}", subject, expiryDate);
        
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        
        logger.debug("JWT token created successfully");
        return token;
    }
    
    public String getUsernameFromToken(String token) {
        logger.debug("Extracting username from JWT token");
        try {
            String username = getClaimFromToken(token, Claims::getSubject);
            logger.debug("Username extracted from token: {}", username);
            return username;
        } catch (ExpiredJwtException e) {
            logger.warn("Attempt to extract username from expired token");
            return e.getClaims().getSubject();
        } catch (Exception e) {
            logger.warn("Error extracting username from token: {}", e.getMessage());
            throw e;
        }
    }
    
    public Date getExpirationDateFromToken(String token) {
        logger.debug("Extracting expiration date from JWT token");
        try {
            Date expirationDate = getClaimFromToken(token, Claims::getExpiration);
            logger.debug("Expiration date extracted: {}", expirationDate);
            return expirationDate;
        } catch (Exception e) {
            logger.warn("Error extracting expiration date from token: {}", e.getMessage());
            throw e;
        }
    }
    
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        logger.debug("Resolving claims from JWT token");
        final Claims claims = getAllClaimsFromToken(token);
        T result = claimsResolver.apply(claims);
        logger.debug("Claim resolved successfully from token");
        return result;
    }
    
    private Claims getAllClaimsFromToken(String token) {
        logger.debug("Parsing all claims from JWT token");
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logger.debug("JWT claims parsed successfully");
            return claims;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw e;
        }
    }
    
    public boolean validateToken(String token, UserDetails userDetails) {
        logger.debug("Validating JWT token for user: {}", userDetails.getUsername());
        try {
            final String username = getUsernameFromToken(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            if (isValid) {
                logger.debug("JWT token is valid for user: {}", username);
            } else {
                logger.warn("JWT token validation failed for user: {}", userDetails.getUsername());
            }
            return isValid;
        } catch (Exception e) {
            logger.warn("JWT token validation error: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isTokenExpired(String token) {
        logger.debug("Checking if JWT token is expired");
        try {
            final Date expiration = getExpirationDateFromToken(token);
            boolean isExpired = expiration.before(new Date());
            logger.debug("JWT token expired: {}, expiration date: {}", isExpired, expiration);
            return isExpired;
        } catch (ExpiredJwtException e) {
            logger.debug("JWT token is already expired");
            return true;
        } catch (Exception e) {
            logger.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
}
