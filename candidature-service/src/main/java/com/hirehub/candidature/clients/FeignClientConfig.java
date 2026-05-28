package com.hirehub.candidature.clients;

import com.hirehub.candidature.config.UserContext;
import com.hirehub.common.constants.JwtClaimNames;
import com.hirehub.common.constants.SecurityConstants;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                requestTemplate.header(SecurityConstants.HEADER_CORRELATION_ID, correlationId);
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                applyJwtHeaders(jwtAuth.getToken(), requestTemplate);
            }

            UserContext.UserInfo user = UserContext.getUser();
            if (user != null) {
                if (user.userId != null) {
                    requestTemplate.header(SecurityConstants.HEADER_USER_ID, user.userId.toString());
                }
                if (user.email != null && !user.email.isBlank()) {
                    requestTemplate.header(SecurityConstants.HEADER_USER_EMAIL, user.email);
                }
                if (user.username != null && !user.username.isBlank()) {
                    requestTemplate.header(SecurityConstants.HEADER_USER_NAME, user.username);
                }
                if (user.role != null && !user.role.isBlank()) {
                    requestTemplate.header(SecurityConstants.HEADER_USER_ROLE, user.role);
                }
                if (user.token != null && !user.token.isBlank()) {
                    requestTemplate.header("Authorization", SecurityConstants.BEARER_PREFIX + user.token);
                }
            }
        };
    }

    private void applyJwtHeaders(Jwt jwt, feign.RequestTemplate requestTemplate) {
        if (jwt == null) {
            return;
        }
        String tokenValue = jwt.getTokenValue();
        if (tokenValue != null && !tokenValue.isBlank()) {
            requestTemplate.header("Authorization", SecurityConstants.BEARER_PREFIX + tokenValue);
        }

        String subject = jwt.getSubject();
        if (subject != null && !subject.isBlank()) {
            requestTemplate.header(SecurityConstants.HEADER_USER_ID, subject);
        }

        String email = jwt.getClaimAsString(JwtClaimNames.EMAIL);
        if (email != null && !email.isBlank()) {
            requestTemplate.header(SecurityConstants.HEADER_USER_EMAIL, email);
            requestTemplate.header(SecurityConstants.HEADER_USER_NAME, email);
        }

        String role = jwt.getClaimAsString(JwtClaimNames.ROLE);
        if (role != null && !role.isBlank()) {
            requestTemplate.header(SecurityConstants.HEADER_USER_ROLE, role);
        }
    }
}

