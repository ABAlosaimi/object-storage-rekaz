package com.rekaz.storage.rekaz_storage.Auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.rekaz.storage.rekaz_storage.Registry.StorageRegistry;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.lang.NonNull;

@Service
public class JWTService {
    
    @Value("${security.jwt.secret-key:not found}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String extractStorageType(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(StorageRegistry storageRegistry) {
        return generateToken(new HashMap<>(), storageRegistry);
    }

    public String generateToken(Map<String, Object> extraClaims, StorageRegistry storageRegistry) {
        return buildToken(extraClaims, storageRegistry, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(Map<String, Object> extraClaims, StorageRegistry storageRegistry, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(storageRegistry.getStorageTpye()) 
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, StorageRegistry storageRegistry) {
        final String storageType = extractStorageType(token);
        return (storageType.equals(storageRegistry.getStorageTpye())) && !isTokenExpired(token) && validateToken(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

   
    private boolean validateToken(String token) {
        try {
            Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey()) 
                    .build()
                    .parseClaimsJws(token);
            return true; 
        } catch (JwtException e) {
            return false; 
        }
    }

    @NonNull
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
