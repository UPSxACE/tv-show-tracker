package com.upsxace.tv_show_tracker.actor;

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

    @Column
    private String tmdbId;

    @Column
    private Long actorId;

    @Column
    private Long tvShowId;

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
}
