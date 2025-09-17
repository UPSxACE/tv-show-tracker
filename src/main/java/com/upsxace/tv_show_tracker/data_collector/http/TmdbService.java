package com.upsxace.tv_show_tracker.data_collector.http;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TmdbService {
    private final TmdbClient tmdbClient;
}
