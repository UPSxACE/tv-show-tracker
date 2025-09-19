package com.upsxace.tv_show_tracker.actor;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "actors")
@Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class Actor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private Long tmdbId;

    @Column
    private String name;

    @Column
    private Double popularity;

    @Column
    private String profileUrl;

    @Column
    private String character;
}
