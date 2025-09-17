package com.upsxace.tv_show_tracker.data_collector.dto;

import lombok.Data;

import java.util.List;

@Data
public class TvShowsResponse {
    private final Integer page;
    private final Integer total_pages;
    private final Long total_results;
    private final List<TvShowDto> results;
}
