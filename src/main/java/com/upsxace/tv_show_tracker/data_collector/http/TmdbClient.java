package com.upsxace.tv_show_tracker.data_collector.http;

import com.upsxace.tv_show_tracker.data_collector.dto.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Client for interacting with the TMDB API.
 * Provides methods to fetch TV genres, TV shows, TV show details, and actor credits.
 * Rate-limited using Resilience4j to avoid exceeding TMDB API limits.
 */
@Component
public class TmdbClient {

    @Value("${tmdb.api-key}")
    private String apiKey;

    RestClient customClient;

    /**
     * Initializes the RestClient with base URL and Authorization header.
     */
    @PostConstruct
    public void init(){
        customClient = RestClient.builder()
                .baseUrl("https://api.themoviedb.org")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    /**
     * Retrieves all TV genres from TMDB.
     *
     * @return list of GenreDto objects or null if none found
     */
    @RateLimiter(name = "tmdb")
    public List<GenreDto> getTvGenreList(){
        var result = customClient.get()
                .uri("/3/genre/tv/list")
                .retrieve()
                .body(TvGenreListResponse.class);

        return result == null ? null : result.getGenres();
    }

    /**
     * Retrieves a specific TV genre by ID.
     *
     * @param id TMDB genre ID
     * @return GenreDto object
     */
    @RateLimiter(name = "tmdb")
    public GenreDto getTvGenre(Long id){
        return customClient.get()
                .uri("/3/genre/{id}", id)
                .retrieve()
                .body(GenreDto.class);
    }

    /**
     * Retrieves a page of discovered TV shows from TMDB, sorted by popularity.
     *
     * @param page page number
     * @return TvShowsResponse containing TV show summaries
     */
    @RateLimiter(name = "tmdb")
    public TvShowsResponse getTvShows(int page){
        return customClient.get()
                .uri(String.format("/3/discover/tv?page=%d&sort_by=popularity.desc", page))
                .retrieve()
                .body(TvShowsResponse.class);
    }

    /**
     * Retrieves detailed information about a TV show by its TMDB ID.
     *
     * @param id TMDB TV show ID
     * @return TvShowDetailsDto with detailed TV show information
     */
    @RateLimiter(name = "tmdb")
    public TvShowDetailsDto getTvShowDetails(long id){
        return customClient.get()
                .uri(String.format("/3/tv/%d", id))
                .retrieve()
                .body(TvShowDetailsDto.class);
    }

    /**
     * Retrieves the cast of a TV show by its TMDB ID.
     *
     * @param id TMDB TV show ID
     * @return list of CastPersonDto objects or null if none found
     */
    @RateLimiter(name = "tmdb")
    public List<CastPersonDto> getTvShowCredits(long id){
        var result = customClient.get()
                .uri(String.format("/3/tv/%d/credits", id))
                .retrieve()
                .body(TvShowCreditsResponse.class);

        return result == null ? null : result.getCast();
    }

    /**
     * Retrieves all TV show credits for a person (actor) by TMDB ID.
     *
     * @param id TMDB person ID
     * @return list of PersonCreditsCastDto objects or null if none found
     */
    @RateLimiter(name = "tmdb")
    public List<PersonCreditsCastDto> getPersonTvShowCredits(long id){
        var result = customClient.get()
                .uri(String.format("/3/person/%d/tv_credits", id))
                .retrieve()
                .body(TvShowPersonCreditsResponse.class);

        return result == null ? null : result.getCast();
    }
}
