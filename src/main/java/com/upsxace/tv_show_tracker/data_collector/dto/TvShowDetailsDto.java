package com.upsxace.tv_show_tracker.data_collector.dto;

import com.upsxace.tv_show_tracker.genre.Genre;
import com.upsxace.tv_show_tracker.tv_show.entity.TvShow;
import com.upsxace.tv_show_tracker.tv_show.entity.TvShowGenre;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TvShowDetailsDto {
    private final Long id;
    private final String name;
    private final List<GenreDto> genres;
    private final String overview;
    private final Double vote_average;
    private final String first_air_date;
    private final String last_air_date;
    private final String poster_path;
    private final String backdrop_path;
    private final Double popularity;
    private final Integer number_of_episodes;
    private final Integer number_of_seasons;
    private final List<TvShowSeasonDto> seasons;
    private final Boolean in_production;

    public TvShow toModel(List<Long> genreIds){
        var tvShow = TvShow.builder()
                .tmdbId(id)
                .name(name)
                .overview(overview)
                .posterUrl("https://image.tmdb.org/t/p/original" + poster_path)
                .popularity(popularity)
                .voteAverage(vote_average)
                .numberOfSeasons(number_of_seasons)
                .numberOfEpisodes(number_of_episodes)
                .firstAirDate(LocalDate.parse(first_air_date))
                .lastAirDate(LocalDate.parse(last_air_date))
                .inProduction(in_production)
                .seasons(seasons.stream().map(TvShowSeasonDto::toModel).toList())
                .build();

        tvShow.setTvShowGenres(
                genreIds.stream()
                        .map(gId -> TvShowGenre.builder()
                                .tvShow(tvShow)
                                .genre(Genre.builder().id(gId).build())
                                .build()
                        )
                        .collect(Collectors.toSet())
        );

        tvShow.getSeasons().forEach(
                s -> s.setTvShow(tvShow)
        );

        return tvShow;
    }
}
