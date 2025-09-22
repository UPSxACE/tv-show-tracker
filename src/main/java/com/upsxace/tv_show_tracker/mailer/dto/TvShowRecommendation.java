package com.upsxace.tv_show_tracker.mailer.dto;

import lombok.Data;

@Data
public class TvShowRecommendation {
    private final String name;
    private final String posterUrl;
    private final String link;
}
