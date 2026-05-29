package com.hirehub.frontend.config;

import com.hirehub.frontend.auth.HirehubUserDetails;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    private final FrontendJwtService frontendJwtService;

    public FeignClientConfig(FrontendJwtService frontendJwtService) {
        this.frontendJwtService = frontendJwtService;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // 1. Propagate correlation ID for distributed tracing
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                requestTemplate.header("X-Correlation-Id", correlationId);
            }

            // 2. Try to forward Authorization header from the incoming HTTP request
            //    (works when the browser request already carries a Bearer token)
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null) {
                    requestTemplate.header("Authorization", authHeader);
                    return;
                }
            }

            // 3. No incoming Bearer token — generate one from the session user context.
            //    The frontend uses form-login (session), not JWT. Microservices need JWT.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                    && authentication.getPrincipal() instanceof HirehubUserDetails userDetails) {
                String token = frontendJwtService.generateToken(userDetails);
                requestTemplate.header("Authorization", "Bearer " + token);
            }
        };
    }
}
