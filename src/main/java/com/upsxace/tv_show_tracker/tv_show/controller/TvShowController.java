package com.upsxace.tv_show_tracker.tv_show.controller;

import com.upsxace.tv_show_tracker.actor.entity.ActorCredit;
import com.upsxace.tv_show_tracker.actor.service.ActorService;
import com.upsxace.tv_show_tracker.tv_show.service.TvShowService;
import com.upsxace.tv_show_tracker.tv_show.graphql.AllTvShowsInput;
import com.upsxace.tv_show_tracker.tv_show.graphql.TvShowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GraphQL controller for TV shows, providing queries for fetching
 * individual shows, all shows, and batch actor credits.
 */
@Controller
@RequiredArgsConstructor
public class TvShowController {

    private final TvShowService tvShowService;
    private final ActorService actorService;

    /**
     * Retrieves a single TV show by its ID.
     *
     * @param id TV show ID
     * @return TV show DTO or null if not found
     */
    @QueryMapping
    public TvShowDto getTvShow(@Argument Long id) {
        return tvShowService.getById(id);
    }

    /**
     * Retrieves all TV shows with optional filtering, sorting, and pagination.
     *
     * @param input Input containing filters, sorting, and pagination info
     * @return Page of TV show DTOs
     */
    @QueryMapping
    public Page<TvShowDto> allTvShows(@Argument AllTvShowsInput input) {
        return tvShowService.getAll(input);
    }

    /**
     * Batch resolver for fetching actor credits for a list of TV shows.
     *
     * @param tvShows List of TV show DTOs
     * @return List of actor credits corresponding to each TV show
     */
    @BatchMapping(typeName = "TvShow")
    public List<List<ActorCredit>> actorCredits(List<TvShowDto> tvShows) {
        var tvShowIds = tvShows.stream().map(TvShowDto::getId).toList();
        var allActorCredits = actorService.getActorCreditsByTvShowIds(tvShowIds);

        Map<Long, List<ActorCredit>> creditsByTvShowId = allActorCredits.stream()
                .collect(Collectors.groupingBy(ActorCredit::getTvShowId));

        return tvShows.stream()
                .map(t -> creditsByTvShowId.getOrDefault(t.getId(), List.of()))
                .toList();
    }
}