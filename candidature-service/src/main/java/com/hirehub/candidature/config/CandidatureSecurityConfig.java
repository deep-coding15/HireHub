package com.hirehub.candidature.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sécurité Spring : permissive au niveau HTTP.
 * La validation du JWT et l'injection des identités sont désormais gérées par
 * l'API Gateway ({@code JwtAuthGatewayFilter}). Ce service lit les headers
 * X-User-* via {@link UserContextHeaderFilter} et applique ses propres contrôles
 * métier via {@link CandidatureSecurityService}.
 */
@Configuration
@EnableWebSecurity
public class CandidatureSecurityConfig {

    @Bean
    public SecurityFilterChain candidatureSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
