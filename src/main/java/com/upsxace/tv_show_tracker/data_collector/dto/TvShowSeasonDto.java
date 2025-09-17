package com.upsxace.tv_show_tracker.data_collector.dto;

import lombok.Data;

@Data
public class TvShowSeasonDto {
    private final Long id;
    private final String name;
    private final Integer season_number;
    private final Long episode_count;
    private final String air_date;
}
