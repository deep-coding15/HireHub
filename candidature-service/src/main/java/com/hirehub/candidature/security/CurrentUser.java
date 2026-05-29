package com.hirehub.candidature.security;

import com.hirehub.candidature.config.UserContext;
import com.hirehub.common.enums.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Accès statique au contexte utilisateur courant.
 * Les données proviennent des headers X-User-* injectés par l'API Gateway
 * et placés dans {@link UserContext} par {@link com.hirehub.candidature.config.UserContextHeaderFilter}.
 */
public final class CurrentUser {

    private CurrentUser() {}

    public static String requireSubject() {
        UserContext.UserInfo user = userOrThrow();
        return user.userId.toString();
    }

    public static UUID requireUserId() {
        return userOrThrow().userId;
    }

    public static String requireEmail() {
        UserContext.UserInfo user = userOrThrow();
        if (user.email == null || user.email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email manquant dans le contexte");
        }
        return user.email;
    }

    public static String requireUsername() {
        return requireEmail();
    }

    public static UserRole requireRole() {
        UserContext.UserInfo user = userOrThrow();
        if (user.role == null || user.role.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rôle manquant dans le contexte");
        }
        try {
            return UserRole.valueOf(user.role);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rôle inconnu : " + user.role);
        }
    }

    public static boolean hasAnyRole(UserRole... roles) {
        UserContext.UserInfo user = UserContext.getUser();
        if (user == null || user.role == null) return false;
        try {
            UserRole current = UserRole.valueOf(user.role);
            for (UserRole r : roles) {
                if (current == r) return true;
            }
        } catch (IllegalArgumentException ignored) {}
        return false;
    }

    public static void requireAnyRole(UserRole... roles) {
        if (!hasAnyRole(roles)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Opération non autorisée pour ce rôle");
        }
    }

    public static UserContext.UserInfo toUserInfo() {
        return userOrThrow();
    }

    // ── Privé ────────────────────────────────────────────────────────────────

    private static UserContext.UserInfo userOrThrow() {
        UserContext.UserInfo user = UserContext.getUser();
        if (user == null || user.userId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentification requise");
        }
        return user;
    }
}
