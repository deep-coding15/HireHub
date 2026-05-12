package com.hirehub.candidature.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sgitu.jwt")
public class JwtProperties {

    private String secret = "";
    private long expirationMs = 86_400_000L; // 24 heures en millisecondes
    //1000 * 60 * 60 * 24 * 7
}
