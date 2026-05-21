package com.hirehub.candidature.security;

import com.hirehub.candidature.config.UserContext;
import com.hirehub.common.constants.JwtClaimNames;
import com.hirehub.common.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public final class CurrentUser {

    private CurrentUser() {}

    public static String requireSubject() {
        Jwt jwt = jwtOrNull();
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentification requise");
        }
        return jwt.getSubject();
    }

    public static UUID requireUserId() {
        try {
            return UUID.fromString(requireSubject());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Identifiant utilisateur invalide dans le token");
        }
    }

    public static String requireEmail() {
        Jwt jwt = jwtOrNull();
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentification requise");
        }
        String email = jwt.getClaimAsString(JwtClaimNames.EMAIL);
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email manquant dans le token");
        }
        return email;
    }

    /**
     * Dans HireHub, le "username" opérationnel correspond à l'email de connexion.
     */
    public static String requireUsername() {
        return requireEmail();
    }

    public static UserRole requireRole() {
        Jwt jwt = jwtOrNull();
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentification requise");
        }
        String claim = jwt.getClaimAsString(JwtClaimNames.ROLE);
        if (claim == null || claim.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role manquant dans le token");
        }
        try {
            return UserRole.valueOf(claim);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role inconnu dans le token");
        }
    }

    public static Boolean requireRecruiterApproved() {
        Jwt jwt = jwtOrNull();
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentification requise");
        }
        return Boolean.TRUE.equals(jwt.getClaimAsBoolean(JwtClaimNames.RECRUTEUR_APPROUVE));
    }

    public static Instant requireIssuedAt() {
        Jwt jwt = jwtOrNull();
        if (jwt == null || jwt.getIssuedAt() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Date d'emission manquante dans le token");
        }
        return jwt.getIssuedAt();
    }

    public static Instant requireExpiresAt() {
        Jwt jwt = jwtOrNull();
        if (jwt == null || jwt.getExpiresAt() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Date d'expiration manquante dans le token");
        }
        return jwt.getExpiresAt();
    }

    public static boolean isTokenValid() {
        Jwt jwt = jwtOrNull();
        return jwt != null
                && jwt.getExpiresAt() != null
                && jwt.getExpiresAt().isAfter(Instant.now());
    }

    public static UserContext.UserInfo toUserInfo() {
        Jwt jwt = jwtOrNull();
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentification requise");
        }

        UUID userId = requireUserId();
        String email = requireEmail();
        String role = requireRole().name();
        Instant issuedAt = jwt.getIssuedAt();
        Instant expiresAt = jwt.getExpiresAt();
        boolean tokenValid = expiresAt != null && expiresAt.isAfter(Instant.now());
        Boolean recruiterApproved = jwt.getClaimAsBoolean(JwtClaimNames.RECRUTEUR_APPROUVE);

        return new UserContext.UserInfo(
                userId,
                email,
                email,
                role,
                null,
                jwt.getTokenValue(),
                issuedAt,
                expiresAt,
                tokenValid,
                recruiterApproved != null && recruiterApproved
        );
    }

    public static boolean hasAnyRole(UserRole... roles) {
        UserRole current;
        try {
            current = requireRole();
        } catch (ResponseStatusException e) {
            return false;
        }
        for (UserRole r : roles) {
            if (current == r) {
                return true;
            }
        }
        return false;
    }

    public static void requireAnyRole(UserRole... roles) {
        UserRole current = requireRole();
        for (UserRole r : roles) {
            if (current == r) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operation non autorisee pour ce role");
    }

    private static Jwt jwtOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        return null;
    }
}
