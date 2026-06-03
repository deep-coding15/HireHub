package com.hirehub.email.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "event-service",
        fallback = EventServiceClientFallback.class
)
public interface EventServiceClientAPI {


}
