package com.hirehub.frontend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Génère un JWT en session pour les utilisateurs déjà connectés (évite de forcer une reconnexion).
 */
@Component
public class JwtSessionEnrichmentFilter extends OncePerRequestFilter {

    private final FrontendJwtService frontendJwtService;

    public JwtSessionEnrichmentFilter(FrontendJwtService frontendJwtService) {
        this.frontendJwtService = frontendJwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SessionAuthSupport.accessToken().isEmpty()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && auth.getPrincipal() instanceof HirehubUserDetails details) {
                SessionAuthSupport.storeAccessToken(frontendJwtService.generateAccessToken(details));
            }
        }
        filterChain.doFilter(request, response);
    }
}
