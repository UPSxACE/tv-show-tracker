package com.upsxace.tv_show_tracker.data_collector.dto;

import com.upsxace.tv_show_tracker.actor.entity.Actor;
import com.upsxace.tv_show_tracker.actor.entity.ActorCredit;
import com.upsxace.tv_show_tracker.common.utils.DateUtils;
import com.upsxace.tv_show_tracker.tv_show.entity.TvShow;
import lombok.Data;

@Data
public class PersonCreditsCastDto {
    private final Long id;
    private final String credit_id;
    private final String name;
    private final String overview;
    private final Double popularity;
    private final String poster_path;
    private final String character;
    private final String first_air_date;
    private final String first_credit_air_date;

    public ActorCredit toModel(TvShow tvShow, Actor actor) {
        return ActorCredit.builder()
                .tmdbId(credit_id)
                .tvShowTmdbId(id)
                .name(name)
                .overview(overview)
                .popularity(popularity)
                .character(character)
                .firstAirDate(DateUtils.safeDateParse(first_air_date))
                .firstCreditAirDate(DateUtils.safeDateParse(first_credit_air_date))
                .tvShow(tvShow)
                .actor(actor)
                .build();

    }
}
