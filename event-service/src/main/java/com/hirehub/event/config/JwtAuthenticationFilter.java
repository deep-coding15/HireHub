package com.hirehub.event.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class JwtAuthenticationFilter implements Filter {

    private final JwtTokenValidator jwtTokenValidator;

    public JwtAuthenticationFilter(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String authHeader = httpRequest.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtTokenValidator.isTokenValid(token)) {
                    UUID userId = jwtTokenValidator.extractUserId(token);
                    String email = jwtTokenValidator.extractEmail(token);
                    String role = jwtTokenValidator.extractRole(token);
                    String fullName = jwtTokenValidator.extractFullName(token);

                    UserContext.setUser(userId, email, role, fullName);
                    log.debug("[JWT] Token valide pour userId={}, email={}", userId, email);
                } else {
                    log.warn("[JWT] Token invalide ou expiré");
                }
            }

            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}

