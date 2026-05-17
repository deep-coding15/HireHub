package com.hirehub.candidature.clients;

import com.hirehub.common.constants.SecurityConstants;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            copyHeader(request, requestTemplate, "Authorization");
            copyHeader(request, requestTemplate, SecurityConstants.HEADER_USER_ID);
            copyHeader(request, requestTemplate, SecurityConstants.HEADER_USER_EMAIL);
            copyHeader(request, requestTemplate, SecurityConstants.HEADER_USER_NAME);
            copyHeader(request, requestTemplate, SecurityConstants.HEADER_USER_ROLE);

            String userName = request.getHeader(SecurityConstants.HEADER_USER_NAME);
            if (userName != null && !userName.isBlank()) {
                requestTemplate.header(SecurityConstants.HEADER_USER_NAME, userName);
            }
        };
    }

    private void copyHeader(HttpServletRequest request, feign.RequestTemplate requestTemplate, String headerName) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isBlank()) {
            requestTemplate.header(headerName, value);
        }
    }
}

