package com.hirehub.email.feign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@Component
@FeignClient(
        name = "offre-service",
        fallback = OffreServiceClientFallback.class
)
public interface OffreServiceClientAPI {
    @GetMapping("/api/offres/{id}")
    OffreInfoDTO getOffreById(@PathVariable("id") String id);
}
