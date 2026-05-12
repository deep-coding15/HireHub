package com.hirehub.candidature.security;

import com.hirehub.common.constants.JwtClaimNames;
import com.hirehub.common.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

public final class CurrentUser {

    private CurrentUser() {}

    public static String requireSubject() {
        Jwt jwt = jwtOrNull();
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentification requise");
        }
        return jwt.getSubject();
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
