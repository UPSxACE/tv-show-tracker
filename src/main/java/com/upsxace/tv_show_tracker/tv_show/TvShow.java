package com.upsxace.tv_show_tracker.tv_show;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tv_shows")
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class TvShow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private Long tmdbId;

    @Column
    private String name;

    @Column
    private String overview;

    @Column
    private String posterUrl;

    @Column
    private Double popularity;

    @Column
    private Double voteAverage;

    @Column
    private Integer numberOfSeasons;

    @Column
    private Integer numberOfEpisodes;

    @Column
    private LocalDate firstAirDate;

    @Column
    private LocalDate lastAirDate;

    @Column
    private Boolean inProduction;
}
