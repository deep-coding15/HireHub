package com.hirehub.email.feign;

import com.hirehub.email.dto.CandidateInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(
        name = "candidature-service",
        fallback = CandidateServiceClientFallback.class
)
public interface CandidateServiceClientAPI {

    @GetMapping("/api/candidats/{id}")
    CandidateInfoDTO getCandidateById(@PathVariable("id") String id);
}

