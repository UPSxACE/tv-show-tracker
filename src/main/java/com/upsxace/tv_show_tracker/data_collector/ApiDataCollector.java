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
import java.time.LocalDateTime;

/**
 * Service responsible for collecting TV show data from the TMDB API.
 * Handles genre collection and periodic discovery of new TV shows.
 * Implements error handling and cooldown periods to avoid overloading the API or database.
 */
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
    private LocalDateTime cooldownUntil;

    /**
     * Retrieves the current size of the database in megabytes.
     *
     * @return current database size in MB
     */
    private int getCurrentDbSizeInMb() {
        String sql = String.format("SELECT pg_database_size('%s');", dbName);
        var result = (Number) entityManager.createNativeQuery(sql).getSingleResult();
        return result.intValue() / 1024 / 1024;
    }

    /**
     * Determines whether the service should continue collecting new TV show data.
     *
     * @return true if discovery is enabled and database size is under the maximum limit
     */
    public boolean shouldContinueCollecting() {
        return discoveryEnabled && getCurrentDbSizeInMb() < dbMaxSizeMb;
    }

    /**
     * Initializes the collector by fetching TMDB genres.
     */
    @PostConstruct
    public void init() {
        tmdbService.collectGenres();
    }

    /**
     * Runs background discovery tasks periodically.
     * Applies error handling and cooldown to prevent excessive requests.
     */
    @Scheduled(fixedDelay = 10000)
    public void backgroundTasks() {
        // Reset error count if cooldown has expired
        if (cooldownUntil != null && LocalDateTime.now().isAfter(cooldownUntil)) {
            errorCount = 0;
            cooldownUntil = null;
        }

        if (cooldownUntil != null) return;

        try {
            if (shouldContinueCollecting()) tmdbService.discover();
        } catch (Exception e) {
            log.error("An error occurred while trying to discover. Error count: {}", errorCount);
            errorCount++;
            if (errorCount >= 3) {
                // After 3 errors, skip page and pause discovery for 2 minutes
                log.info("More than 2 errors have occurred. Skipping page and cooling down discovery for 2 minutes.");
                tmdbService.skipPage();
                cooldownUntil = LocalDateTime.now().plus(Duration.ofMinutes(2));
            }
        }
    }
}
