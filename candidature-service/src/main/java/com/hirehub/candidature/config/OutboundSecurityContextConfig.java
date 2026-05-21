package com.hirehub.candidature.config;

import com.hirehub.common.constants.JwtClaimNames;
import com.hirehub.common.constants.SecurityConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.function.BiConsumer;

@Configuration
public class OutboundSecurityContextConfig {

    @Bean
    /**intercepteur de requete pour la propagation en sortie */
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new SecurityContextRestTemplateInterceptor());
        return restTemplate;
    }

    @Bean
    /**filtre exhange pour la propagation en sortie*/
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder().filter(securityContextExchangeFilter());
    }

    @Bean
    public ExchangeFilterFunction securityContextExchangeFilter() {
        return (request, next) -> {
            ClientRequest.Builder builder = ClientRequest.from(request);
            applyHeaders(builder::header);
            return next.exchange(builder.build());
        };
    }

    private static class SecurityContextRestTemplateInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public org.springframework.http.client.ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {
            HttpRequestWrapper wrapper = new HttpRequestWrapper(request);
            applyHeaders(wrapper.getHeaders()::set);
            return execution.execute(wrapper, body);
        }
    }

    private static void applyHeaders(BiConsumer<String, String> headerConsumer) {
        UserContext.UserInfo user = UserContext.getUser();
        if (user != null) {
            if (user.token != null && !user.token.isBlank()) {
                headerConsumer.accept(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER_PREFIX + user.token);
            }
            if (user.userId != null) {
                headerConsumer.accept(SecurityConstants.HEADER_USER_ID, user.userId.toString());
            }
            if (user.email != null && !user.email.isBlank()) {
                headerConsumer.accept(SecurityConstants.HEADER_USER_EMAIL, user.email);
            }
            if (user.username != null && !user.username.isBlank()) {
                headerConsumer.accept(SecurityConstants.HEADER_USER_NAME, user.username);
            }
            if (user.role != null && !user.role.isBlank()) {
                headerConsumer.accept(SecurityConstants.HEADER_USER_ROLE, user.role);
            }
        }

        Jwt jwt = currentJwt();
        if (jwt == null) {
            return;
        }

        if (user == null || user.token == null || user.token.isBlank()) {
            String tokenValue = jwt.getTokenValue();
            if (tokenValue != null && !tokenValue.isBlank()) {
                headerConsumer.accept(HttpHeaders.AUTHORIZATION, SecurityConstants.BEARER_PREFIX + tokenValue);
            }
        }

        if (user == null || user.userId == null) {
            String subject = jwt.getSubject();
            if (subject != null && !subject.isBlank()) {
                headerConsumer.accept(SecurityConstants.HEADER_USER_ID, subject);
            }
        }

        if (user == null || user.email == null || user.email.isBlank()) {
            String email = jwt.getClaimAsString(JwtClaimNames.EMAIL);
            if (email != null && !email.isBlank()) {
                headerConsumer.accept(SecurityConstants.HEADER_USER_EMAIL, email);
                headerConsumer.accept(SecurityConstants.HEADER_USER_NAME, email);
            }
        }

        if (user == null || user.role == null || user.role.isBlank()) {
            String role = jwt.getClaimAsString(JwtClaimNames.ROLE);
            if (role != null && !role.isBlank()) {
                headerConsumer.accept(SecurityConstants.HEADER_USER_ROLE, role);
            }
        }
    }

    private static Jwt currentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        return null;
    }
}
