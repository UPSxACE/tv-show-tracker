package com.upsxace.tv_show_tracker.data_collector;

import com.upsxace.tv_show_tracker.data_collector.http.TmdbService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiDataCollector {
    @Value("${app.db.name}")
    private String dbName;
    @Value("${app.db.max-size}")
    private Integer dbMaxSizeMb;
    @Value("${tmdb.api-key")
    private String tmdbApiKey;

    @PersistenceContext
    private EntityManager entityManager;

    private final TmdbService tmdbService;

    private int getCurrentDbSizeInMb(){
        String sql = String.format("SELECT pg_database_size('%s');", dbName);
        var result = (Number) entityManager.createNativeQuery(sql).getSingleResult();
        return result.intValue() / 1024 / 1024;
    }

    public boolean shouldContinueCollecting(){
        return getCurrentDbSizeInMb() < dbMaxSizeMb;
    }
}
