package com.upsxace.tv_show_tracker.tv_show.graphql;

import com.upsxace.tv_show_tracker.actor.ActorCredit;
import com.upsxace.tv_show_tracker.genre.Genre;
import com.upsxace.tv_show_tracker.tv_show.Season;
import lombok.Data;

import java.util.List;

@Data
public class TvShowDto {
    private final Long id;
    private final String name;
    private final String overview;
    private final String posterUrl;
    private final Double popularity;
    private final Double voteAverage;
    private final Integer numberOfSeasons;
    private final Integer numberOfEpisodes;
    private final String firstAirDate;
    private final String lastAirDate;
    private final Boolean inProduction;
    private final List<Season> seasons;
    private final List<Genre> genres;
    private final List<ActorCredit> actorCredits;
}
