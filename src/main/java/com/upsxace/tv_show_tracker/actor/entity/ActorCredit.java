package com.upsxace.tv_show_tracker.actor.entity;

import com.upsxace.tv_show_tracker.tv_show.entity.TvShow;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "actor_credits")
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class ActorCredit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(name = "tv_show_id", insertable = false, updatable = false)
    private Long tvShowId;

    @Column(name = "actor_id", insertable = false, updatable = false)
    private Long actorId;

    @Column
    private String tmdbId;

    @Column
    private Long tvShowTmdbId;

    @Column
    private String name;

    @Column
    private String overview;

    @Column
    private Double popularity;

    @Column
    private String character;

    @Column
    private LocalDate firstAirDate;

    @Column
    private LocalDate firstCreditAirDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tv_show_id")
    private TvShow tvShow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private Actor actor;
}
