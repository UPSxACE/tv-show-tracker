package com.upsxace.tv_show_tracker.tv_show;

import com.upsxace.tv_show_tracker.genre.Genre;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tv_show_genres")
@Getter @Setter
@Builder @AllArgsConstructor @NoArgsConstructor
public class TvShowGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tv_show_id")
    private TvShow tvShow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    private Genre genre;
}
