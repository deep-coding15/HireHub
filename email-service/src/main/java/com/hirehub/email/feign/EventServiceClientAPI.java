package com.hirehub.email.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "event-service", url = "http://localhost:8084", fallback = EventServiceClientFallback.class)
public interface EventServiceClientAPI {


}
