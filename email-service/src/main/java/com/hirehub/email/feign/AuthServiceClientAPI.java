package com.hirehub.email.feign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@Component
@FeignClient(
        name = "auth-service",
        fallback = AuthServiceClientFallback.class
)
public interface AuthServiceClientAPI {
    @GetMapping("/api/users/{id}")
    UserInfoDTO getUserById(@PathVariable("id") String id);
}
