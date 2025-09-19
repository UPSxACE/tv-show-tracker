package com.upsxace.tv_show_tracker.data_collector.dto;

import com.upsxace.tv_show_tracker.common.utils.DateUtils;
import com.upsxace.tv_show_tracker.tv_show.Season;
import com.upsxace.tv_show_tracker.tv_show.TvShow;
import lombok.Data;

@Data
public class TvShowSeasonDto {
    private final Long id;
    private final String name;
    private final Integer season_number;
    private final Integer episode_count;
    private final String air_date;

    public Season toModel(){
        return Season.builder()
                .tmdbId(id)
                .seasonNumber(season_number)
                .name(name)
                .episodeCount(episode_count)
                .airDate(DateUtils.safeDateParse(air_date))
                .build();
    }
}
