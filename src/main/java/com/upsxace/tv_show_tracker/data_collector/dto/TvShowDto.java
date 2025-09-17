package com.upsxace.tv_show_tracker.data_collector.dto;

import lombok.Data;

import java.util.List;

@Data
public class TvShowDto {
    private final Long id;
    private final String name;
    private final List<Integer> genre_ids;
    private final String overview;
    private final Double vote_average;
    private final String first_air_date;
    private final String poster_path;
    private final String backdrop_path;
    private final Double popularity;
}
