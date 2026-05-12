package com.hirehub.auth.security;

import com.hirehub.auth.model.UserAccount;
import com.hirehub.common.constants.JwtClaimNames;
import com.hirehub.common.enums.UserRole;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(
            SecretKey hirehubJwtSecretKey,
            @Value("${hirehub.jwt.expiration-seconds:3600}") long expirationSeconds
    ) {
        this.signingKey = hirehubJwtSecretKey;
        this.expirationSeconds = expirationSeconds;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String generateAccessToken(UserAccount user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSeconds);
        boolean recruteurApprouve = user.getRole() == UserRole.RECRUTEUR && user.isRecruiterApproved();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(JwtClaimNames.EMAIL, user.getEmail())
                .claim(JwtClaimNames.ROLE, user.getRole().name())
                .claim(JwtClaimNames.RECRUTEUR_APPROUVE, recruteurApprouve)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey)
                .compact();
    }
}
