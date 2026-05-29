package com.hirehub.frontend.offre;

import com.hirehub.frontend.auth.HirehubUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class RecruiterContext {

    private RecruiterContext() {
    }

    public static HirehubUserDetails requireRecruiter() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Utilisateur non connecte");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof HirehubUserDetails userDetails)) {
            throw new IllegalStateException("Session invalide : reconnectez-vous");
        }
        return userDetails;
    }
}
