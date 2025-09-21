package com.upsxace.tv_show_tracker.common.jwt;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtConfig {
    private String secret;
    private TokenProperties accessToken;
    private TokenProperties refreshToken;
    private CookieProperties cookie;

    @Getter
    @Setter
    public static class TokenProperties {
        private Long duration;
    }

    @Getter
    @Setter
    public static class CookieProperties {
        private String domain;
        private boolean secure;
    }
}
