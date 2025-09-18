package com.upsxace.tv_show_tracker.data_collector.http;

import com.upsxace.tv_show_tracker.data_collector.dto.*;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TmdbClient {
    @Value("${tmdb.api-key}")
    private String apiKey;

    RestClient customClient;

    @PostConstruct
    public void init(){
        customClient = RestClient.builder()
                .baseUrl("https://api.themoviedb.org")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @RateLimiter(name = "tmdb")
    public List<GenreDto> getTvGenreList(){
        var result = customClient.get()
                    .uri("/3/genre/tv/list")
                    .retrieve()
                    .body(TvGenreListResponse.class);

        return result == null ? null : result.getGenres();
    }

    @RateLimiter(name = "tmdb")
    public TvShowsResponse getTvShows(int page){
        return customClient.get()
                .uri(String.format("/3/discover/tv?page=%d&sort_by=popularity.desc", page))
                .retrieve()
                .body(TvShowsResponse.class);
    }

    @RateLimiter(name = "tmdb")
    public TvShowDetailsDto getTvShowDetails(long id){
        return customClient.get()
                .uri(String.format("/3/tv/%d", id))
                .retrieve()
                .body(TvShowDetailsDto.class);
    }

    @RateLimiter(name = "tmdb")
    public List<CastPersonDto> getTvShowCredits(long id){
        var result = customClient.get()
                .uri(String.format("/3/tv/%d/credits", id))
                .retrieve()
                .body(TvShowCreditsResponse.class);

        return result == null ? null : result.getCast();
    }

    @RateLimiter(name = "tmdb")
    public List<PersonCreditsCastDto> getPersonTvShowCredits(long id){
        var result = customClient.get()
                .uri(String.format("/3/person/%d/tv_credits", id))
                .retrieve()
                .body(TvShowPersonCreditsResponse.class);

        return result == null ? null : result.getCast();
    }
}
