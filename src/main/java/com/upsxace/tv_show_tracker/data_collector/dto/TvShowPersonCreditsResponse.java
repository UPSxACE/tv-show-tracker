package com.upsxace.tv_show_tracker.data_collector.dto;

import lombok.Data;

import java.util.List;

@Data
public class TvShowPersonCreditsResponse {
    private final List<PersonCreditsCastDto> cast;
}
