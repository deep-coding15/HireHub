package com.hirehub.frontend.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public final class SessionAuthSupport {

    private SessionAuthSupport() {}

    public static void storeAccessToken(String token) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            servletAttrs.getRequest().getSession(true).setAttribute(SessionAuthKeys.ACCESS_TOKEN, token);
        }
    }

    public static Optional<String> accessToken() {
        return currentSession()
                .map(s -> s.getAttribute(SessionAuthKeys.ACCESS_TOKEN))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(t -> !t.isBlank());
    }

    public static void clearAccessToken() {
        currentSession().ifPresent(s -> s.removeAttribute(SessionAuthKeys.ACCESS_TOKEN));
    }

    private static Optional<HttpSession> currentSession() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return Optional.ofNullable(servletAttrs.getRequest().getSession(false));
        }
        return Optional.empty();
    }
}
