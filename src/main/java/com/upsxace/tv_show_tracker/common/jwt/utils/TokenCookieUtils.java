package com.upsxace.tv_show_tracker.common.jwt.utils;

import com.upsxace.tv_show_tracker.common.jwt.JwtConfig;
import org.springframework.http.ResponseCookie;

public class TokenCookieUtils {
    private static ResponseCookie.ResponseCookieBuilder applyDefaults(ResponseCookie.ResponseCookieBuilder builder, JwtConfig jwtConfig) {
        return builder
                .domain(jwtConfig.getCookie().getDomain())
                .path("/")
                .secure(jwtConfig.getCookie().isSecure())
                .sameSite("Strict");
    }

    public static String getAccessTokenCookie(JwtConfig jwtConfig, String accessToken) {
        return applyDefaults(
                ResponseCookie.from("accessToken", accessToken)
                        .httpOnly(false)
                        .maxAge(jwtConfig.getAccessToken().getDuration()),
                jwtConfig
        ).build().toString();
    }

    public static String getRefreshTokenOookie(JwtConfig jwtConfig, String refreshToken) {
        return applyDefaults(
                ResponseCookie.from("refreshToken", refreshToken)
                        .httpOnly(true) // prevent JavaScript access
                        .maxAge(jwtConfig.getRefreshToken().getDuration()),
                jwtConfig
        ).build().toString();
    }

    public static String getAccessTokenDeleteCookie(JwtConfig jwtConfig){
        return applyDefaults(
                ResponseCookie.from("accessToken", "")
                        .httpOnly(false)
                        .maxAge(0),
                jwtConfig
        ).build().toString();
    }

    public static String getRefreshTokenDeleteOookie(JwtConfig jwtConfig) {
        return applyDefaults(
                ResponseCookie.from("refreshToken", "")
                        .httpOnly(true) // prevent JavaScript access
                        .maxAge(0),
                jwtConfig
        ).build().toString();
    }
}
