package com.hirehub.frontend.auth;

import com.hirehub.frontend.notification.AuthNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class LogoutNotificationHandler implements LogoutHandler {

    private final AuthNotificationService authNotificationService;

    public LogoutNotificationHandler(AuthNotificationService authNotificationService) {
        this.authNotificationService = authNotificationService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof HirehubUserDetails details) {
            try {
                authNotificationService.publishLogout(details);
            } catch (Exception ignored) {
                // Notification best-effort
            }
        }
    }
}
