package com.upsxace.tv_show_tracker.common.jwt.utils;

import com.upsxace.tv_show_tracker.common.jwt.JwtConfig;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
public class RefreshResult {
    private final String accessToken;

    public String getAccessTokenCookie(JwtConfig jwtConfig){
        return ResponseCookie.from("accessToken", accessToken)
                .domain(jwtConfig.getCookie().getDomain())
                .httpOnly(true) // prevent JavaScript access
                .path("/")
                .maxAge(jwtConfig.getAccessToken().getDuration())
                .secure(false)
                .sameSite("Strict")
                .build()
                .toString();
    }
}
