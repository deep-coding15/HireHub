package com.hirehub.frontend.config;

import com.hirehub.common.constants.SecurityConstants;
import com.hirehub.frontend.auth.HirehubUserDetails;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
            if (authentication != null
                    && authentication.getPrincipal() instanceof HirehubUserDetails user) {
                requestTemplate.header(SecurityConstants.HEADER_USER_ID, user.getId().toString());
                requestTemplate.header(SecurityConstants.HEADER_USER_EMAIL, user.getUsername());
                requestTemplate.header(SecurityConstants.HEADER_USER_ROLE, user.getRole().name());
                requestTemplate.header(SecurityConstants.HEADER_USER_NAME, user.getUsername());
            }
        };
    }
}
