package com.upsxace.tv_show_tracker.tv_show.controller;

import com.upsxace.tv_show_tracker.actor.entity.ActorCredit;
import com.upsxace.tv_show_tracker.actor.service.ActorService;
import com.upsxace.tv_show_tracker.common.jwt.UserContext;
import com.upsxace.tv_show_tracker.tv_show.service.TvShowService;
import com.upsxace.tv_show_tracker.tv_show.graphql.AllTvShowsInput;
import com.upsxace.tv_show_tracker.tv_show.graphql.TvShowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.*;
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

    /**
     * Batch mapping resolver for GraphQL to determine if a list of TvShows are favorites
     * for the current user.
     *
     * <p>This returns a {@code List<Optional<Boolean>>} instead of {@code List<Boolean>}
     * because @BatchMapping in Spring GraphQL does not allow null elements in the returned list.
     * Wrapping Boolean values in Optional allows us to explicitly represent three states:
     * <ul>
     *     <li>{@code Optional.of(true)} — the show is a favorite</li>
     *     <li>{@code Optional.of(false)} — the show is not a favorite</li>
     *     <li>{@code Optional.empty()} — the favorite state is unknown (e.g., no user context)</li>
     * </ul>
     *
     * @param tvShows  the list of TvShow DTOs to resolve favorites for
     * @param userCtx  optional user context (may be empty if no user is logged in)
     * @return a list of Optional<Boolean> indicating favorite status for each TvShow
     */
    @BatchMapping(typeName = "TvShow")
    public List<Optional<Boolean>> favorite(
            List<TvShowDto> tvShows,
            @ContextValue Optional<UserContext> userCtx
    ) {

        if (userCtx.isEmpty())
            return Collections.nCopies(tvShows.size(), Optional.empty());

        var tvShowIds = tvShows.stream()
                .map(TvShowDto::getId)
                .toList();

        var userFavorites = tvShowService.getUserFavoritesByShowId(tvShowIds, userCtx.get());

        Map<Long, Optional<Boolean>> favoriteByShowId = userFavorites.stream()
                .collect(Collectors.toMap(
                        u -> u.getTvShow().getId(),
                        u -> Optional.of(true)
                ));

        return tvShows.stream()
                .map(t -> favoriteByShowId.getOrDefault(t.getId(), Optional.of(false)))
                .toList();
    }
}