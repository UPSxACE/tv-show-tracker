package com.upsxace.tv_show_tracker.data_collector.dto;

import lombok.Data;

@Data
public class CastPersonDto {
    private final Long id;
    private final String known_for_department;
    private final String name;
    private final Double popularity;
    private final String profile_path;
    private final String character;
}
