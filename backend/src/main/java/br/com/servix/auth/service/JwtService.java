package br.com.servix.auth.service;

import br.com.servix.auth.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${servix.security.jwt.secret}")
    private String jwtSecret;

    @Value("${servix.security.jwt.access-expiration}")
    private long accessExpirationSeconds;

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Map<String, Object> claims = Map.of(
                "email", user.getEmail(),
                "companyId", user.getCompany().getId().toString(),
                "profiles", user.getProfiles().stream()
                        .map(profile -> profile.getName().name())
                        .toList());
        return Jwts.builder()
                .subject(user.getId().toString())
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpirationSeconds)))
                .signWith(getSigningKey())
                .compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public boolean isTokenValid(String token, ServixUserDetails userDetails) {
        UUID userId = extractUserId(token);
        Date expiration = extractAllClaims(token).getExpiration();
        return userId.equals(userDetails.getUserId()) && expiration.after(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        try {
            byte[] secretBytes = MessageDigest.getInstance("SHA-256")
                    .digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(secretBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Não foi possível inicializar chave JWT", ex);
        }
    }
}
