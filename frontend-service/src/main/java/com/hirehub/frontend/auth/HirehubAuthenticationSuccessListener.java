package com.hirehub.frontend.auth;

import com.hirehub.frontend.notification.AuthNotificationService;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class HirehubAuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final FrontendJwtService frontendJwtService;
    private final AuthNotificationService authNotificationService;

    public HirehubAuthenticationSuccessListener(
            FrontendJwtService frontendJwtService,
            AuthNotificationService authNotificationService
    ) {
        this.frontendJwtService = frontendJwtService;
        this.authNotificationService = authNotificationService;
    }

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (authentication.getPrincipal() instanceof HirehubUserDetails details) {
            String token = frontendJwtService.generateAccessToken(details);
            SessionAuthSupport.storeAccessToken(token);

            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                try {
                    authNotificationService.publishLogin(details, servletAttrs.getRequest());
                } catch (Exception ignored) {
                    // Notification best-effort
                }
            }
        }
    }
}
