package com.hirehub.frontend.notification;

import com.hirehub.common.notification.RabbitMQConstants;
import com.hirehub.frontend.auth.HirehubUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthNotificationService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FrontendEmailEventPublisher publisher;

    public AuthNotificationService(FrontendEmailEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishLogin(HirehubUserDetails user, HttpServletRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("loginDateTime", LocalDateTime.now().format(FORMATTER));
        payload.put("ipAddress", clientIp(request));
        payload.put("userAgent", request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "Inconnu");

        publisher.publish(
                "AUTH.LOGIN",
                user.getUsername(),
                user.getFullName(),
                RabbitMQConstants.ROUTING_USER_AUTHENTIFICATION_LOGIN,
                payload
        );
    }

    public void publishLogout(HirehubUserDetails user) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("logoutDateTime", LocalDateTime.now().format(FORMATTER));

        publisher.publish(
                "AUTH.LOGOUT",
                user.getUsername(),
                user.getFullName(),
                RabbitMQConstants.ROUTING_USER_AUTHENTIFICATION_LOGOUT,
                payload
        );
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "Inconnu";
    }
}
