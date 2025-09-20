package com.upsxace.tv_show_tracker.genre;

import com.upsxace.tv_show_tracker.tv_show.TvShowGenre;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

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

    @OneToMany(mappedBy = "genre")
    private Set<TvShowGenre> tvShowGenres;
}
