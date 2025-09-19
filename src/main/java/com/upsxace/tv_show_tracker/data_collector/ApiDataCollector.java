package com.upsxace.tv_show_tracker.data_collector;

import com.upsxace.tv_show_tracker.data_collector.http.TmdbService;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiDataCollector {
    @Value("${app.db.name}")
    private String dbName;
    @Value("${app.db.max-size}")
    private Integer dbMaxSizeMb;
    @Value("${tmdb.api-key}")
    private String tmdbApiKey;
    @Value("${discovery.enabled}")
    private Boolean discoveryEnabled;

    @PersistenceContext
    private EntityManager entityManager;

    private final TmdbService tmdbService;

    private int errorCount;
    private LocalDate cooldownUntil;

    private int getCurrentDbSizeInMb() {
        String sql = String.format("SELECT pg_database_size('%s');", dbName);
        var result = (Number) entityManager.createNativeQuery(sql).getSingleResult();
        return result.intValue() / 1024 / 1024;
    }

    public boolean shouldContinueCollecting() {
        return discoveryEnabled && getCurrentDbSizeInMb() < dbMaxSizeMb;
    }

    @PostConstruct
    public void init() {
        tmdbService.collectGenres();
    }

    @Scheduled(fixedDelay = 10000)
    public void backgroundTasks() {
        if (cooldownUntil != null && LocalDate.now().isAfter(cooldownUntil)) {
            // when cooldown time has passed, reset count
            errorCount = 0;
            cooldownUntil = null;
        }

        if (cooldownUntil != null) return;

        try {
            if(shouldContinueCollecting()) tmdbService.discover();
        } catch (Exception e) {
            log.error("An error occurred while trying to discover. Error count: {}", errorCount);
            errorCount++;
            if (errorCount >= 3) {
                // if more than 3 errors occur, stop sending requests for 2 minutes
                log.info("More than 2 errors have occurred. Resetting state and cooling down discovery for 2 minutes.");
                tmdbService.resetState();
                cooldownUntil = LocalDate.now().plus(Duration.ofMinutes(2));
            }
        }
    }
}
