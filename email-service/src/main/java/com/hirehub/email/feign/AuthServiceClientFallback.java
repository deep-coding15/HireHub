package com.hirehub.email.feign;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class AuthServiceClientFallback implements AuthServiceClientAPI {
    @Override
    public UserInfoDTO getUserById(String id) {
        log.warn("[Feign Fallback] Impossible de récupérer l'utilisateur {} depuis auth-service", id);
        return new UserInfoDTO(id, "user@unknown.com", "Unknown", "User", "UNKNOWN");
    }
}
