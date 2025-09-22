package com.upsxace.tv_show_tracker.tv_show;

import com.upsxace.tv_show_tracker.actor.ActorCredit;
import com.upsxace.tv_show_tracker.user.UserFavoriteTvShow;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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

    @OneToMany(mappedBy = "tvShow", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private List<Season> seasons;

    @OneToMany(mappedBy = "tvShow", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TvShowGenre> tvShowGenres;

    @OneToMany(mappedBy = "tvShow")
    private List<ActorCredit> actorCredits;

    @OneToMany(mappedBy = "tvShow")
    private Set<UserFavoriteTvShow> favoritedBy;
}
