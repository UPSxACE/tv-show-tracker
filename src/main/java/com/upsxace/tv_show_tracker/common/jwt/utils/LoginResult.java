package com.upsxace.tv_show_tracker.common.jwt.utils;

import lombok.Data;

@Data
public class LoginResult {
    private final String refreshToken;
    private final String accessToken;

    public LoginResult(String refreshToken, String accessToken){
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }
}
