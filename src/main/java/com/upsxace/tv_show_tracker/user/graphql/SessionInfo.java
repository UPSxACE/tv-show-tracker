package com.upsxace.tv_show_tracker.user.graphql;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class SessionInfo {
    private final UUID id;
    private final String username;
    private final String avatarUrl;
}
