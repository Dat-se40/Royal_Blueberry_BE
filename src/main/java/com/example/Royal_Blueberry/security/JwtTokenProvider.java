package com.example.Royal_Blueberry.security;

import com.example.Royal_Blueberry.util.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;


@Slf4j
@Component
public class JwtTokenProvider {

    private static final String OAUTH_STATE_TYPE = "oauth_state";

    @Value("${jwt.access_key}")
    private String accessKey;

    @Value("${jwt.refresh_key}")
    private String refreshKey;

    @Value("${jwt.access-expiration}")
    private int accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private int refreshTokenExpiration;


    public String generateAccessToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (long) accessTokenExpiration * 60 * 1000);

        return Jwts.builder()
                .subject(userDetails.getUser().getId())
                .claim("email", userDetails.getUsername())
                .claim("role", userDetails.getUser().getRole().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getKey(TokenType.ACCESS))
                .compact();
    }

    public String generateRefreshToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (long) refreshTokenExpiration * 60 * 1000);

        return Jwts.builder()
                .subject(userDetails.getUser().getId())
                .claim("email", userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getKey(TokenType.REFRESH))
                .compact();
    }

    public String generateOAuthStateToken(String provider) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 5L * 60 * 1000);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(provider)
                .claim("type", OAUTH_STATE_TYPE)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getKey(TokenType.ACCESS))
                .compact();
    }

    public boolean validateOAuthStateToken(String token, String provider) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey(TokenType.ACCESS))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return OAUTH_STATE_TYPE.equals(claims.get("type"))
                    && provider.equals(claims.getSubject());
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid OAuth state signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("OAuth state token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("OAuth state token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("OAuth state token is empty: {}", e.getMessage());
        }
        return false;
    }


    public String getUserIdFromToken(String token, TokenType type) {
        return parseClaims(token, type).getSubject();
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public boolean validateToken(String token, TokenType type) {
        try {
            parseClaims(token, type);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }


    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token, TokenType.REFRESH);
            return claims != null;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token, TokenType type) {
        return Jwts.parser()
                .verifyWith(getKey(type))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey(TokenType type) {
        if (type.equals(TokenType.ACCESS))
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
        else return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
    }
}
