package com.hirehub.candidature.config;

import com.hirehub.common.constants.SecurityConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Propage les headers X-User-* dans les appels RestTemplate sortants (non-Feign).
 * Simplifié après passage au modèle gateway-trust : plus de JWT Bearer à propager.
 */
@Configuration
public class OutboundSecurityContextConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new UserContextRestTemplateInterceptor());
        return restTemplate;
    }

    private static class UserContextRestTemplateInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request,
                                            byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            HttpRequestWrapper wrapper = new HttpRequestWrapper(request);
            UserContext.UserInfo user = UserContext.getUser();
            if (user != null) {
                if (user.userId != null) {
                    wrapper.getHeaders().set(SecurityConstants.HEADER_USER_ID, user.userId.toString());
                }
                if (user.email != null && !user.email.isBlank()) {
                    wrapper.getHeaders().set(SecurityConstants.HEADER_USER_EMAIL, user.email);
                }
                if (user.role != null && !user.role.isBlank()) {
                    wrapper.getHeaders().set(SecurityConstants.HEADER_USER_ROLE, user.role);
                }
            }
            return execution.execute(wrapper, body);
        }
    }
}
