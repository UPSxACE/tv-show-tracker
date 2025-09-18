package com.upsxace.tv_show_tracker.data_collector.dto;

import com.upsxace.tv_show_tracker.genre.Genre;
import lombok.Data;

@Data
public class GenreDto {
    private final Long id;
    private final String name;

    public Genre toModel(){
        return Genre
                .builder()
                .tmdbId(id)
                .name(name)
                .build();
    }
}
