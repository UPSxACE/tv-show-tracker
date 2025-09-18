package com.upsxace.tv_show_tracker.genre;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "genres")
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private Long tmdbId;

    @Column
    private String name;
}
