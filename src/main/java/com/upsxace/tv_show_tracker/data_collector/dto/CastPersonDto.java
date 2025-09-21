package com.upsxace.tv_show_tracker.data_collector.dto;

import com.upsxace.tv_show_tracker.actor.Actor;
import com.upsxace.tv_show_tracker.actor.ActorCredit;
import com.upsxace.tv_show_tracker.tv_show.TvShow;
import lombok.Data;

@Data
public class CastPersonDto {
    private final Long id;
    private final String known_for_department;
    private final String name;
    private final Double popularity;
    private final String profile_path;
    private final String character;

    public Actor toActorModel(){
        return Actor.builder()
                .tmdbId(id)
                .name(name)
                .popularity(popularity)
                .profileUrl("https://image.tmdb.org/t/p/original" + profile_path)
                .build();
    }

    public ActorCredit toActorCreditModel(TvShow tvShow, Actor actor){
        return ActorCredit.builder()
                .name(name)
                .popularity(popularity)
                .character(character)
                .tvShowTmdbId(tvShow.getTmdbId())
                .actor(actor)
                .tvShow(tvShow)
                .build();
    }
}
