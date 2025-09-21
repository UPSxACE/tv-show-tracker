package com.upsxace.tv_show_tracker.user.graphql;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class JwtResponse {
    private final String accessToken;
}
