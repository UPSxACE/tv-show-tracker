package com.upsxace.tv_show_tracker.tv_show;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "seasons")
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class Season {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private Long tmdbId;

    @Column
    private Integer seasonNumber;

    @Column
    private String name;

    @Column
    private Integer episodeCount;

    @Column
    private LocalDate airDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tv_show_id")
    private TvShow tvShow;
}
