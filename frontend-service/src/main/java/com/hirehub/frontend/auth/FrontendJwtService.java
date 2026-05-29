package com.hirehub.frontend.auth;

import com.hirehub.common.constants.JwtClaimNames;
import com.hirehub.common.enums.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class FrontendJwtService {

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public FrontendJwtService(
            @Value("${hirehub.jwt.secret}") String rawSecret,
            @Value("${hirehub.jwt.expiration-seconds:3600}") long expirationSeconds
    ) {
        byte[] keyBytes = rawSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("hirehub.jwt.secret must be at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateAccessToken(HirehubUserDetails user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSeconds);
        boolean recruteurApprouve = user.getRole() == UserRole.RECRUTEUR && user.isRecruiterApproved();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(JwtClaimNames.EMAIL, user.getUsername())
                .claim(JwtClaimNames.ROLE, user.getRole().name())
                .claim(JwtClaimNames.RECRUTEUR_APPROUVE, recruteurApprouve)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey)
                .compact();
    }
}
