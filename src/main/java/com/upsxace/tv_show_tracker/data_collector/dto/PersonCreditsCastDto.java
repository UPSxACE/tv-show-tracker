package com.upsxace.tv_show_tracker.data_collector.dto;

import lombok.Data;

@Data
public class PersonCreditsCastDto {
    private final Long id;
    private final String name;
    private final Double popularity;
    private final String poster_path;
    private final String character;
    private final String first_air_date;
}
