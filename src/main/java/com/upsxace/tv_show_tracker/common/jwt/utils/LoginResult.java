package com.upsxace.tv_show_tracker.common.jwt.utils;

import com.upsxace.tv_show_tracker.common.jwt.JwtConfig;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
public class LoginResult {
    private final String refreshToken;
    private final String accessToken;

    public LoginResult(String refreshToken, String accessToken){
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

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

    public String getRefreshTokenOookie(JwtConfig jwtConfig){
        return ResponseCookie.from("refreshToken", refreshToken)
                .domain(jwtConfig.getCookie().getDomain())
                .httpOnly(true) // prevent JavaScript access
                .path("/")
                .maxAge(jwtConfig.getRefreshToken().getDuration())
                .secure(true)
                .sameSite("Strict")
                .build()
                .toString();
    }
}
